package hohserg.soulkeeper.worldgen

import hohserg.soulkeeper.Configuration
import hohserg.soulkeeper.blocks.{BlockSoulkeeperPlant, GrowStage}
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.fml.common.IWorldGenerator

import java.util.Random

object SoulkeeperPlantGenerator extends IWorldGenerator {
  override def generate(random: Random, chunkX: Int, chunkZ: Int, world: World, chunkGenerator: IChunkGenerator, chunkProvider: IChunkProvider): Unit = {

    def randomPlantState =
      BlockSoulkeeperPlant.getDefaultState.withProperty(BlockSoulkeeperPlant.growProperty, GrowStage.values()(random.nextInt(GrowStage.values().length)))

    if (world.provider.getDimension == 0) {
      val (startX, startZ) = (chunkX << 4, chunkZ << 4)

      WorldGenUtils.generateInArea(new BlockPos(startX + 8, 14, startZ + 8), new BlockPos(startX + 15 + 8, 57, startZ + 15 + 8), pos => {
        if (Configuration.soulkeeperGenWhitelist.contains(world.getBlockState(pos).getBlock)
          && world.isAirBlock(pos.up())
          && onWater(world, pos)
          && (random.nextInt(Configuration.soulkeeperRarity) == 0)) {
          WorldGenUtils.setBlockAndNotifyAdequately(notify = false, world, pos.up(), randomPlantState)
        }
      })
    }
  }

  private def onWater(world: World, pos: BlockPos): Boolean = {
    import collection.JavaConverters._
    BlockPos.getAllInBox(pos.add(-1, 0, -1), pos.add(1, 0, 1)).asScala
      .map(pos1 => world.getBlockState(pos1))
      .map(state => state.getBlock == Blocks.WATER && state.getValue(BlockLiquid.LEVEL) == 0)
      .count(identity) >= 3
  }
}
