package hohserg.soulkeeper.worldgen

import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object WorldGenUtils {

  def generateInAreaBreakly(start: BlockPos, end: BlockPos, generate: BlockPos => Boolean): Unit = {
    val pos = new BlockPos.MutableBlockPos()
    for (x <- start.getX to end.getX) {
      for (z <- start.getZ to end.getZ) {
        for (y <- start.getY to end.getY) {
          pos.setPos(x, y, z)
          if (!generate(pos))
            return
        }
      }
    }
  }

  def generateInArea(start: BlockPos, end: BlockPos, generate: BlockPos => Unit): Unit =
    generateInAreaBreakly(start, end, generate.andThen(_ => true))

  def setBlockAndNotifyAdequately(notify: Boolean, world: World, pos: BlockPos, state: IBlockState): Unit = {
    if (notify) world.setBlockState(pos, state, 3)
    else {
      val flag = if (net.minecraftforge.common.ForgeModContainer.fixVanillaCascading) 2 | 16 else 2 //Forge: With bit 5 unset, it will notify neighbors and load adjacent chunks.
      world.setBlockState(pos, state, flag)
    }
  }

}
