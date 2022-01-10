package hohserg.soulkeeper.blocks

import hohserg.soulkeeper.Configuration.xpPerRhinestonePowderInfuse
import hohserg.soulkeeper.Main
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.block.{Block, BlockFalling, SoundType}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.{Entity, EntityLivingBase}
import net.minecraft.init.Items
import net.minecraft.item.{IItemPropertyGetter, Item, ItemBlock, ItemStack}
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{NonNullList, ResourceLocation}
import net.minecraft.world.{Explosion, IBlockAccess, World}
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import scala.collection.JavaConverters._
import scala.language.implicitConversions

@EventBusSubscriber(modid = Main.modid)
object BlockDarkRhinestonePowder extends BlockFalling(Material.SAND) with ItemBlockProvider with RhColor {

  /*infuse property*/
  lazy val infuseProperty = PropertyInteger.create("infuse", 0, 15)

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, infuseProperty)

  override def getMetaFromState(state: IBlockState): Int = state.getValue(infuseProperty)

  override def getStateFromMeta(meta: Int): IBlockState = withInfuse(meta)

  setDefaultState(withInfuse(0))

  def withInfuse(v: Int) = getDefaultState.withProperty[Integer, Integer](infuseProperty, v)

  /*difference to infused version*/
  private def choiceValue[A](state: IBlockState, rock: A, powder: A): A =
    if (state.getValue(infuseProperty) == 15)
      rock
    else
      powder

  override def getBlockHardness(state: IBlockState, worldIn: World, pos: BlockPos): Float =
    choiceValue(state, 5, 1)


  override def getExplosionResistance(world: World, pos: BlockPos, exploder: Entity, explosion: Explosion): Float =
    choiceValue(world.getBlockState(pos), 10, 5)

  override def getSoundType(state: IBlockState, world: World, pos: BlockPos, entity: Entity): SoundType =
    choiceValue(state, SoundType.GLASS, SoundType.SAND)

  setHarvestLevel("pickaxe", 1, withInfuse(15))
  for (i <- 0 to 14)
    setHarvestLevel("shovel", 0, withInfuse(i))

  override def getSubBlocks(itemIn: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
    items.add(new ItemStack(this))
    items.add(new ItemStack(this, 1, 15))
  }

  /*infuse logic*/
  override def getCollisionBoundingBox(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    choiceValue(state, Block.FULL_BLOCK_AABB, new AxisAlignedBB(0.01, 0.01, 0.01, 0.99, 0.99, 0.99))

  override def onEntityCollidedWithBlock(worldIn: World, pos: BlockPos, state: IBlockState, entityIn: Entity): Unit = {
    if (!worldIn.isRemote)
      if (state.getValue(infuseProperty) < 15)
        entityIn match {
          case orb: CustomEntityXPOrb if orb.isEntityAlive =>
            if (orb.xpValue >= xpPerRhinestonePowderInfuse) {
              val otherAmount = infuse(worldIn, pos, orb.xpValue)
              if (otherAmount > 0)
                orb.setXpValue(otherAmount)
              else
                orb.setDead()
            } else {
              val entities = worldIn.getEntitiesWithinAABB(classOf[CustomEntityXPOrb], new AxisAlignedBB(pos)).asScala.filter(_.isEntityAlive)
              val sum = entities.map(e => e.xpValue).sum
              if (sum > xpPerRhinestonePowderInfuse) {
                val otherAmount = infuse(worldIn, pos, sum)
                entities.foreach(_.setDead())
                if (otherAmount > 0)
                  worldIn.spawnEntity(new CustomEntityXPOrb(new EntityXPOrb(worldIn, pos.getX, pos.getY, pos.getZ, otherAmount)))
              }
            }
          case _ =>
        }
  }

  def infuse(world: World, pos: BlockPos, amount: Int): Int = {
    val currentInfuse = world.getBlockState(pos).getValue(infuseProperty)
    val currentInfuseAmount = currentInfuse * xpPerRhinestonePowderInfuse

    val nextInfuse = (currentInfuseAmount + amount) / xpPerRhinestonePowderInfuse
    val nextInfuseAmount = nextInfuse * xpPerRhinestonePowderInfuse

    if (currentInfuse < nextInfuse) {
      world.setBlockState(pos, withInfuse(math.min(15, nextInfuse)))
    }

    currentInfuseAmount + amount - nextInfuseAmount
  }

  @SubscribeEvent
  def attachCapaToItem(event: AttachCapabilitiesEvent[ItemStack]): Unit = {
    val stack = event.getObject
    if (Item.getItemFromBlock(this) != Items.AIR && stack.getItem == Item.getItemFromBlock(this)) {
      event.addCapability(new ResourceLocation(Main.modid, "capa_xp_container"), new CapabilityXPContainer {
        override def getXp: Int = stack.getItemDamage * xpPerRhinestonePowderInfuse

        override def setXp(amount: Int): Unit = stack.setItemDamage(amount / xpPerRhinestonePowderInfuse)

        override def getXpCapacity: Int = xpPerRhinestonePowderInfuse * 15
      })
    }
  }

  override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = {
    worldIn.setBlockState(pos, state.withProperty[Integer, Integer](infuseProperty, Math.min(15, stack.getItemDamage)))
  }

  override def damageDropped(state: IBlockState): Int = state.getValue(infuseProperty)

  override def getItemBlock: Item = new ItemBlock(this) {
    setHasSubtypes(true)
    addPropertyOverride(new ResourceLocation("enchanted"), new IItemPropertyGetter {
      override def apply(stack: ItemStack, worldIn: World, entityIn: EntityLivingBase): Float =
        if (stack.getItemDamage == 15) 1 else 0
    })

    override def getUnlocalizedName(stack: ItemStack): String =
      if (stack.getItemDamage == 15)
        "tile.block_dark_rhinestone"
      else
        "tile.block_dark_rhinestone_powder"
  }
}
