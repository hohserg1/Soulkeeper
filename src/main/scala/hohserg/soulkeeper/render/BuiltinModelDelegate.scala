package hohserg.soulkeeper.render

import javax.vecmath.Matrix4f
import net.minecraft.client.renderer.block.model.{BuiltInModel, IBakedModel, ItemCameraTransforms}
import org.apache.commons.lang3.tuple
import org.apache.commons.lang3.tuple.Pair

class BuiltinModelDelegate(base: IBakedModel) extends BuiltInModel(base.getItemCameraTransforms, base.getOverrides) {
  override def handlePerspective(cameraTransformType: ItemCameraTransforms.TransformType): tuple.Pair[_ <: IBakedModel, Matrix4f] = {
    val matrix4f = base.handlePerspective(cameraTransformType).getRight
    Pair.of(this, matrix4f)
  }
}
