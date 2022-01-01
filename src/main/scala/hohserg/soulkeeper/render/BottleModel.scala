package hohserg.soulkeeper.render

import java.util

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.{BakedQuad, IBakedModel}
import net.minecraft.util.EnumFacing

class BottleModel(cork: IBakedModel, bottle: IBakedModel) extends BakedModelDelegate(bottle) {

  val cache: Map[EnumFacing, util.List[BakedQuad]] = (EnumFacing.values() :+ null).map(side => {
    val r = new util.ArrayList[BakedQuad]()

    r.addAll(cork.getQuads(null, side, 0))
    r.addAll(bottle.getQuads(null, side, 0))

    side -> r
  }).toMap

  override def getQuads(state: IBlockState, side: EnumFacing, rand: Long): util.List[BakedQuad] =
    cache(side)

}
