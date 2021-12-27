package hohserg.soulkeeper.render

import java.util

import com.google.common.collect.ImmutableList
import hohserg.soulkeeper.Main
import hohserg.soulkeeper.render.FilledBottleModel._
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.{BakedQuad, IBakedModel}
import net.minecraft.client.renderer.texture.SimpleTexture
import net.minecraft.client.renderer.{CurrentTextureIdLens, RenderItem}
import net.minecraft.util.EnumFacing

class FilledBottleModel(bottleModel: IBakedModel, contentModel: IBakedModel) extends BakedModelDelegate(bottleModel) {
  override def getQuads(state: IBlockState, side: EnumFacing, rand: Long): util.List[BakedQuad] = {
    if (side == null) {
      if (isGlintRendering)
        contentModel.getQuads(state, side, rand)
      else {
        val r = new util.ArrayList[BakedQuad]()

        r.addAll(contentModel.getQuads(state, side, rand))
        if (Minecraft.getMinecraft.player.isSneaking)
          r.addAll(bottleModel.getQuads(state, side, rand))

        r
      }
    } else
      ImmutableList.of()
  }

}

object FilledBottleModel {
  val contentTextureName = Main.modid + ":items/item_filled_bottle_content"

  lazy val glintTextureId: Int =
    Option(Minecraft.getMinecraft.getTextureManager.getTexture(RenderItem.RES_ITEM_GLINT))
      .getOrElse({
        val t = new SimpleTexture(RenderItem.RES_ITEM_GLINT)
        Minecraft.getMinecraft.getTextureManager.loadTexture(RenderItem.RES_ITEM_GLINT, t)
        t
      }).getGlTextureId


  def isGlintRendering: Boolean =
    CurrentTextureIdLens.get == glintTextureId
}
