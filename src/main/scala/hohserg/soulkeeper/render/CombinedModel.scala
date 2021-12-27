package hohserg.soulkeeper.render

import java.util

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.{BakedQuad, IBakedModel}
import net.minecraft.util.EnumFacing

class CombinedModel(first: IBakedModel, second: IBakedModel) extends BakedModelDelegate(first) {

  val cache: Map[EnumFacing, util.List[BakedQuad]] = (EnumFacing.values() :+ null).map(side => {
    val r = new util.ArrayList[BakedQuad]()

    r.addAll(first.getQuads(null, side, 0))
    r.addAll(second.getQuads(null, side, 0))

    side -> r
  }).toMap

  override def getQuads(state: IBlockState, side: EnumFacing, rand: Long): util.List[BakedQuad] =
    cache(side)

}
