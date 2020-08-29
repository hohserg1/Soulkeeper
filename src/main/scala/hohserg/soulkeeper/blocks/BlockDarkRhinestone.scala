package hohserg.soulkeeper.blocks

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, SoundType}
import net.minecraft.util.{BlockRenderLayer, EnumBlockRenderType}

object BlockDarkRhinestone extends Block(Material.GLASS) {

  setHardness(1)
  setResistance(10)
  setSoundType(SoundType.GLASS)
  setHarvestLevel("pickaxe", 0)

  override def getRenderType(state: IBlockState): EnumBlockRenderType = super.getRenderType(state)

  override def getBlockLayer: BlockRenderLayer = BlockRenderLayer.TRANSLUCENT

}
