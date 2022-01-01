package hohserg.soulkeeper.blocks

import net.minecraft.block.Block
import net.minecraft.block.material.MapColor
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

trait RhColor {
  this: Block =>
  override def getMapColor(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): MapColor =
    MapColor.CYAN_STAINED_HARDENED_CLAY

}
