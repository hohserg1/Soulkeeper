package hohserg.soulkeeper.blocks

import java.util.Random

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.items.ItemTinyRhinestoneDust
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, SoundType}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{BlockRenderLayer, EnumParticleTypes}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object BlockDarkRhinestoneStalactite extends Block(Material.GLASS) {
  setHardness(0.15f)
  setResistance(0.05f)
  setSoundType(SoundType.GLASS)
  setHarvestLevel("pickaxe", 0)
  lightValue = 1

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    new AxisAlignedBB(0.4, 0.1, 0.4, 0.6, 1.0, 0.6)

  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB = null

  override def neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos): Unit = {
    if (worldIn.isAirBlock(pos.up))
      worldIn.setBlockToAir(pos)
  }

  override def getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ItemTinyRhinestoneDust

  override def quantityDropped(state: IBlockState, fortune: Int, random: Random): Int = random.nextInt((fortune + 1) * 3) + 1

  override def canSilkHarvest(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer): Boolean = true

  override def canPlaceBlockAt(worldIn: World, pos: BlockPos): Boolean = {
    val upState = worldIn.getBlockState(pos.up)
    val upBlock = upState.getBlock
    upBlock.getMaterial(upState) == Material.ROCK && upBlock.isFullBlock(upState)
  }

  override def isFullBlock(state: IBlockState): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false

  @SideOnly(Side.CLIENT)
  override def getBlockLayer: BlockRenderLayer = BlockRenderLayer.TRANSLUCENT

  /* water drip */
  override def randomDisplayTick(stateIn: IBlockState, worldIn: World, pos: BlockPos, rand: Random): Unit =
    worldIn.spawnParticle(EnumParticleTypes.WATER_DROP, pos.getX, pos.getY, pos.getZ, 0, 0, 0)
}
