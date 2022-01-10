package hohserg.soulkeeper.blocks

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{BlockFalling, SoundType}
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.IBlockAccess

object BlockRhPowderOrb extends BlockFalling(Material.SAND) with RhColor {
  setHardness(0.5f)
  setResistance(5)
  setSoundType(SoundType.SAND)
  setHarvestLevel("shovel", 0)

  override def isFullBlock(state: IBlockState): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false

  val pixel = 1d / 16

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    new AxisAlignedBB(pixel, 0, pixel, 1d - pixel, 1d - pixel * 2, 1d - pixel)

}
