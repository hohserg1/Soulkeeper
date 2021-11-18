package hohserg.soulkeeper.blocks

import hohserg.soulkeeper.entities.CustomEntityXPOrb
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.{BlockStateContainer, IBlockState}
import net.minecraft.block.{BlockFalling, SoundType}
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.{IBlockAccess, World}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

object BlockDarkRhinestonePowder extends BlockFalling(Material.SAND) {

  setHardness(1)
  setResistance(5)
  setSoundType(SoundType.SAND)
  setHarvestLevel("shovel", 0)

  private final val xpPerInfuseState = 10

  /*infuse property*/
  lazy val infuseProperty = PropertyInteger.create("infuse", 0, 15)

  override def createBlockState(): BlockStateContainer = new BlockStateContainer(this, infuseProperty)

  override def getMetaFromState(state: IBlockState): Int = state.getValue(infuseProperty)

  override def getStateFromMeta(meta: Int): IBlockState = withInfuse(meta)

  setDefaultState(withInfuse(0))

  private def withInfuse(v: Int) = getDefaultState.withProperty[Integer, Integer](infuseProperty, v)

  /*infuse logic*/
  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    new AxisAlignedBB(0.01, 0.01, 0.01, 0.99, 0.99, 0.99)


  override def onEntityCollidedWithBlock(worldIn: World, pos: BlockPos, state: IBlockState, entityIn: Entity): Unit = {
    if (!worldIn.isRemote)
      entityIn match {
        case orb: CustomEntityXPOrb if orb.isEntityAlive =>
          if (orb.xpValue >= xpPerInfuseState) {
            val otherAmount = infuse(worldIn, pos, orb.xpValue)
            if (otherAmount > 0)
              orb.setXpValue(otherAmount)
            else
              orb.setDead()
          } else {
            val entities = worldIn.getEntitiesWithinAABB(classOf[CustomEntityXPOrb], new AxisAlignedBB(pos)).asScala.filter(_.isEntityAlive)
            val sum = entities.map(e => e.xpValue).sum
            if (sum > xpPerInfuseState) {
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
    val currentInfuseAmount = currentInfuse * xpPerInfuseState

    val nextInfuse = (currentInfuseAmount + amount) / xpPerInfuseState
    val nextInfuseAmount = nextInfuse * xpPerInfuseState

    if (currentInfuse < nextInfuse) {
      if (nextInfuse > 15)
        world.setBlockState(pos, BlockDarkRhinestone.getDefaultState)
      else
        world.setBlockState(pos, withInfuse(math.min(15, nextInfuse)))
    }

    currentInfuseAmount + amount - nextInfuseAmount
  }

}
