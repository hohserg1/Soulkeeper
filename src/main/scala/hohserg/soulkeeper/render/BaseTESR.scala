package hohserg.soulkeeper.render

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{GlStateManager, Tessellator}
import net.minecraft.tileentity.TileEntity

class BaseTESR[A <: TileEntity] extends TileEntitySpecialRenderer[A] {


  def drawString(text: String, x: Double, y: Double, z: Double, yaw: Float): Unit = {

    val fontRendererIn = Minecraft.getMinecraft.fontRenderer

    val verticalShift = 0
    val viewerPitch = rendererDispatcher.entityPitch
    val isThirdPersonFrontal = false

    GlStateManager.pushMatrix()
    GlStateManager.translate(x, y, z)
    GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F)
    GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F)
    GlStateManager.rotate((if (isThirdPersonFrontal) -1 else 1).toFloat * viewerPitch, 1.0F, 0.0F, 0.0F)
    val scale = 0.025f / 3
    GlStateManager.scale(-scale, -scale, scale)
    GlStateManager.disableLighting()
    GlStateManager.depthMask(false)

    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
    val i = fontRendererIn.getStringWidth(text) / 2
    GlStateManager.disableTexture2D()
    val tessellator = Tessellator.getInstance
    val bufferbuilder = tessellator.getBuffer
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
    bufferbuilder.pos((-i - 1).toDouble, (-1 + verticalShift).toDouble, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
    bufferbuilder.pos((-i - 1).toDouble, (8 + verticalShift).toDouble, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
    bufferbuilder.pos((i + 1).toDouble, (8 + verticalShift).toDouble, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
    bufferbuilder.pos((i + 1).toDouble, (-1 + verticalShift).toDouble, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
    tessellator.draw()
    GlStateManager.enableTexture2D()

    GlStateManager.depthMask(true)
    fontRendererIn.drawString(text, -fontRendererIn.getStringWidth(text) / 2, verticalShift, -1)
    GlStateManager.enableLighting()
    GlStateManager.disableBlend()
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
    GlStateManager.popMatrix()
  }

}
