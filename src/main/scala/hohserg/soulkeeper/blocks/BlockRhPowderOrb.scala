package hohserg.soulkeeper.blocks

import hohserg.soulkeeper.items.{ItemRhinestoneDust, ItemTinyRhinestoneDust}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{BlockFalling, SoundType}
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.{IBlockAccess, World}

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

  override def onEndFalling(world: World, pos: BlockPos, p_176502_3_ : IBlockState, p_176502_4_ : IBlockState): Unit = {
    world.setBlockToAir(pos)


    val dustAmount = 4
    val largePileCount = world.rand.nextInt(4)
    val tinyPileCount = dustAmount * 4 - largePileCount * 4

    for (_ <- 1 to tinyPileCount)
      dropItem(world, pos, new ItemStack(ItemTinyRhinestoneDust, 1))

    for (_ <- 1 to largePileCount)
      dropItem(world, pos, new ItemStack(ItemRhinestoneDust, 1))
  }


  private def dropItem(world: World, pos: BlockPos, droppedItem: ItemStack): Unit =
    if (!droppedItem.isEmpty) {
      val entityitem = new EntityItem(world, pos.getX + 0.5, pos.getY, pos.getZ + 0.5, droppedItem) {
        override def searchForOtherItemsNearby(): Unit =
          if (!cannotPickup)
            super.searchForOtherItemsNearby()
      }
      entityitem.setPickupDelay(40)
      entityitem.motionX = world.rand.nextGaussian() * 0.1
      entityitem.motionZ = world.rand.nextGaussian() * 0.1
      entityitem.motionY = world.rand.nextDouble() * 0.5

      world.spawnEntity(entityitem)
    }

}
