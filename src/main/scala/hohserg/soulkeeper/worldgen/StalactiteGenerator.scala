package hohserg.soulkeeper.worldgen

import java.util.Random

import hohserg.soulkeeper.Configuration
import hohserg.soulkeeper.blocks.BlockDarkRhinestoneStalactite
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.fml.common.IWorldGenerator

object StalactiteGenerator extends IWorldGenerator {

  override def generate(random: Random, chunkX: Int, chunkZ: Int, world: World, chunkGenerator: IChunkGenerator, chunkProvider: IChunkProvider): Unit = {
    def setStalactite(pos1: BlockPos): Unit =
      WorldGenUtils.setBlockAndNotifyAdequately(notify = false, world, pos1, BlockDarkRhinestoneStalactite.getDefaultState)

    def aroundAir(pos1: BlockPos): Int =
      EnumFacing.HORIZONTALS.map(pos1.offset).map(world.isAirBlock).count(identity)


    if (world.provider.getDimension == 0) {
      val (startX, startZ) = (chunkX << 4, chunkZ << 4)

      WorldGenUtils.generateInArea(new BlockPos(startX, 14, startZ), new BlockPos(startX + 15, 57, startZ + 15), pos => {
        val state = world.getBlockState(pos)
        if ((state.getBlock == Blocks.WATER)
          && state.getValue(BlockLiquid.LEVEL) == 0
          && world.isAirBlock(pos.up(1))
          && world.isAirBlock(pos.up(2))
        ) {
          if (!world.isAirBlock(pos.up(3))
            && aroundAir(pos.up(2)) >= 3
            && Configuration.rhinestoneStalactiteGenWhitelist.contains(world.getBlockState(pos.up(3)).getBlock)
            && (random.nextInt(Configuration.darkCrystalStalactiteRarity) == 0)) {
            setStalactite(pos.up(2))
          } else {
            if (!world.isAirBlock(pos.up(4))
              && aroundAir(pos.up(3)) >= 3
              && Configuration.rhinestoneStalactiteGenWhitelist.contains(world.getBlockState(pos.up(4)).getBlock)
              && (random.nextInt(Configuration.darkCrystalStalactiteRarity) == 0)) {
              setStalactite(pos.up(3))
            } else {
              if (!world.isAirBlock(pos.up(5))
                && aroundAir(pos.up(4)) >= 3
                && Configuration.rhinestoneStalactiteGenWhitelist.contains(world.getBlockState(pos.up(5)).getBlock)
                && (random.nextInt(Configuration.darkCrystalStalactiteRarity) == 0)) {
                setStalactite(pos.up(4))
              }
            }
          }
        }
      })
    }
  }
}
