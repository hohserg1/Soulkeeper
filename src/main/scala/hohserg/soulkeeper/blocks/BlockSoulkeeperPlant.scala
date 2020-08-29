package hohserg.soulkeeper.blocks

import java.util.Random

import hohserg.soulkeeper.Configuration
import hohserg.soulkeeper.capability.ExpInChunkProvider
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.items.ItemTinyRhinestoneDust
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.{Blocks, Enchantments, Items}
import net.minecraft.item.Item
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{BlockRenderLayer, EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}

object BlockSoulkeeperPlant extends Block(Material.PLANTS) {
  /** grow property **/
  lazy val growProperty = PropertyEnum.create[GrowStage]("grow", classOf[GrowStage])

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, growProperty)

  override def getMetaFromState(state: IBlockState): Int = state.getValue(growProperty).ordinal()

  override def getStateFromMeta(meta: Int): IBlockState = getDefaultState.withProperty(growProperty, GrowStage.values()(meta))

  setDefaultState(getDefaultState.withProperty(growProperty, GrowStage.Empty))

  /** grow mechanics **/
  setTickRandomly(true)

  override def randomTick(worldIn: World, pos: BlockPos, state: IBlockState, random: Random): Unit = {
    if (!worldIn.isRemote) {
      val currentStage = state.getValue(growProperty)
      if (currentStage != GrowStage.Ripe && random.nextInt(math.max(pos.getY, 11) * (worldIn.getLight(pos) + 1)) == 0 && worldIn.getLight(pos) <= 5) {
        val nextStage = GrowStage.values()(currentStage.ordinal() + 1)
        if (ExpInChunkProvider.getCapability(worldIn, pos).consumeXP(10) || random.nextFloat() < Configuration.withoutXPGrowChance)
          worldIn.setBlockState(pos, state.withProperty(growProperty, nextStage))
      }
    }
  }

  private def canBlockStayHere(worldIn: World, pos: BlockPos): Boolean =
    allowedSoil.contains(worldIn.getBlockState(pos.down()).getBlock)

  override def canPlaceBlockAt(worldIn: World, pos: BlockPos): Boolean =
    super.canPlaceBlockAt(worldIn, pos) && canBlockStayHere(worldIn, pos)

  override def neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos): Unit =
    if (!canBlockStayHere(worldIn, pos))
      worldIn.setBlockToAir(pos)

  val allowedSoil = Set(Blocks.STONE, BlockDarkRhinestone)

  /** drop **/
  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (worldIn.getBlockState(pos).getValue(growProperty) == GrowStage.Ripe) {
      worldIn.setBlockState(pos, getDefaultState.withProperty(growProperty, GrowStage.Empty))
      worldIn.spawnEntity(new CustomEntityXPOrb(new EntityXPOrb(worldIn, pos.getX + hitX, pos.getY + hitX, pos.getZ + hitZ, Configuration.soulkeeperXPDrop)))
      true
    } else
      false
  }

  override def getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ItemTinyRhinestoneDust

  override def quantityDropped(state: IBlockState, fortune: Int, random: Random): Int = {
    val stage = state.getValue(growProperty)
    (stage.baseDropCount + random.nextDouble() * stage.additionalDropCount).toInt
  }

  override def canSilkHarvest(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer): Boolean =
    player.getHeldItemMainhand.getItem == Items.SHEARS && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand) >= 1

  /** other **/
  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    state.getValue(growProperty).boundingBox

  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB = Block.NULL_AABB

  override def getBlockLayer: BlockRenderLayer = BlockRenderLayer.TRANSLUCENT


}
