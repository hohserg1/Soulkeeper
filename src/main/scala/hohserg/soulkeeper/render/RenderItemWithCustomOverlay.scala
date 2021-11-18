package hohserg.soulkeeper.render

import java.util

import hohserg.soulkeeper.items.tools.RhTool
import net.minecraft.client.Minecraft.getMinecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer._
import net.minecraft.client.renderer.block.model.{BakedQuad, IBakedModel, ItemCameraTransforms}
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.world.World

class RenderItemWithCustomOverlay(base: RenderItem) extends RenderItem(getMinecraft.getTextureManager, getMinecraft.modelManager, getMinecraft.itemColors) {

  private def renderSpecialBar(stack: ItemStack, xPosition: Int, yPosition: Int) = {
    if (!stack.isEmpty && stack.getItem.isInstanceOf[RhTool]) {
      GlStateManager.disableLighting()
      GlStateManager.disableDepth()
      GlStateManager.disableTexture2D()
      GlStateManager.disableAlpha()
      GlStateManager.disableBlend()

      val durability = stack.getMaxDamage - stack.getItemDamage
      val xp = RhTool.getXp(stack)
      val max = stack.getMaxDamage

      val barSize: Int = 13

      def barProgress(v: Int): Int =
        (v.toFloat / max * barSize).round

      val p1 = barProgress(xp)
      val p2 = barProgress(durability)

      drawRect(xPosition + 2, yPosition + 12.5, 13, 3, 0, 0, 0, 255)
      drawRect(xPosition + 2, yPosition + 14, p2, 1, 0, 255, 0, 255)
      drawRect(xPosition + 2, yPosition + 12.5, p1, 1, 255, 157, 0, 255)

      GlStateManager.enableBlend()
      GlStateManager.enableAlpha()
      GlStateManager.enableTexture2D()
      GlStateManager.enableLighting()
      GlStateManager.enableDepth()
    }
  }

  override def renderItemOverlayIntoGUI(fr: FontRenderer, stack: ItemStack, xPosition: Int, yPosition: Int, text: String): Unit = {
    renderSpecialBar(stack, xPosition, yPosition)

    base.renderItemOverlayIntoGUI(fr, stack, xPosition, yPosition, text)
  }

  override def renderItemOverlays(fr: FontRenderer, stack: ItemStack, xPosition: Int, yPosition: Int): Unit = {
    renderSpecialBar(stack, xPosition, yPosition)

    base.renderItemOverlays(fr, stack, xPosition, yPosition)
  }

  private def drawRect(x: Int, y: Double, width: Int, height: Double, red: Int, green: Int, blue: Int, alpha: Int): Unit = {
    val renderer: BufferBuilder = Tessellator.getInstance.getBuffer
    renderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
    renderer.pos((x + 0).toDouble, (y + 0).toDouble, 0.0D).color(red, green, blue, alpha).endVertex()
    renderer.pos((x + 0).toDouble, (y + height).toDouble, 0.0D).color(red, green, blue, alpha).endVertex()
    renderer.pos((x + width).toDouble, (y + height).toDouble, 0.0D).color(red, green, blue, alpha).endVertex()
    renderer.pos((x + width).toDouble, (y + 0).toDouble, 0.0D).color(red, green, blue, alpha).endVertex()
    Tessellator.getInstance.draw()
  }


  override def getItemModelMesher: ItemModelMesher = base.getItemModelMesher

  override def renderItem(stack: ItemStack, model: IBakedModel): Unit =
    base.renderItem(stack, model)

  override def renderQuads(renderer: BufferBuilder, quads: util.List[BakedQuad], color: Int, stack: ItemStack): Unit =
    base.renderQuads(renderer, quads, color, stack)

  override def shouldRenderItemIn3D(stack: ItemStack): Boolean =
    base.shouldRenderItemIn3D(stack)

  override def renderItem(stack: ItemStack, cameraTransformType: ItemCameraTransforms.TransformType): Unit =
    base.renderItem(stack, cameraTransformType)

  override def getItemModelWithOverrides(stack: ItemStack, worldIn: World, entitylivingbaseIn: EntityLivingBase): IBakedModel =
    base.getItemModelWithOverrides(stack, worldIn, entitylivingbaseIn)

  override def renderItem(stack: ItemStack, entitylivingbaseIn: EntityLivingBase, transform: ItemCameraTransforms.TransformType, leftHanded: Boolean): Unit =
    base.renderItem(stack, entitylivingbaseIn, transform, leftHanded)

  override def renderItemIntoGUI(stack: ItemStack, x: Int, y: Int): Unit =
    base.renderItemIntoGUI(stack, x, y)

  override def renderItemAndEffectIntoGUI(p_184391_1_ : EntityLivingBase, p_184391_2_ : ItemStack, p_184391_3_ : Int, p_184391_4_ : Int): Unit =
    base.renderItemAndEffectIntoGUI(p_184391_1_, p_184391_2_, p_184391_3_, p_184391_4_)

  override def renderItemAndEffectIntoGUI(stack: ItemStack, xPosition: Int, yPosition: Int): Unit =
    base.renderItemAndEffectIntoGUI(stack, xPosition, yPosition)
}
