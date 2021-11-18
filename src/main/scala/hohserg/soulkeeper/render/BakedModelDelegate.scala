package hohserg.soulkeeper.render

import java.util

import javax.vecmath.Matrix4f
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.{BakedQuad, IBakedModel, ItemCameraTransforms, ItemOverrideList}
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.EnumFacing
import org.apache.commons.lang3.tuple.Pair

class BakedModelDelegate(base: IBakedModel) extends IBakedModel {

  override def getQuads(state: IBlockState, side: EnumFacing, rand: Long): util.List[BakedQuad] = base.getQuads(state, side, rand)

  override def isAmbientOcclusion: Boolean = base.isAmbientOcclusion

  override def isAmbientOcclusion(state: IBlockState): Boolean = base.isAmbientOcclusion(state)

  override def isGui3d: Boolean = base.isGui3d

  override def isBuiltInRenderer: Boolean = base.isBuiltInRenderer

  override def getParticleTexture: TextureAtlasSprite = base.getParticleTexture

  override def getOverrides: ItemOverrideList = base.getOverrides

  override def getItemCameraTransforms: ItemCameraTransforms = base.getItemCameraTransforms

  override def handlePerspective(cameraTransformType: ItemCameraTransforms.TransformType): Pair[_ <: IBakedModel, Matrix4f] = {
    val matrix4f = base.handlePerspective(cameraTransformType).getRight
    Pair.of(this, matrix4f)
  }
}
