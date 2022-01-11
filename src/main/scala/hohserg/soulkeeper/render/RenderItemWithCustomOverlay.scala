package hohserg.soulkeeper.render

import hohserg.soulkeeper.items.tools.RhTool
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer._
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack

class RenderItemWithCustomOverlay(base: RenderItem) extends RenderItemDelegate(base) {


  private def renderSpecialBar(stack: ItemStack, xPosition: Int, yPosition: Int) = {
    stack.getItem match {
      case tool: RhTool =>
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()

        val durability = stack.getMaxDamage - stack.getItemDamage
        val xp = tool.getXp(stack)
        val max = stack.getMaxDamage

        val barSize: Int = 13

        def barProgress(v: Int): Double =
          v.toFloat / max * barSize

        val p1 = barProgress(xp)
        //println(xp, p1)
        val p2 = barProgress(durability)

        drawRect(xPosition + 2, yPosition + 12.5, 13, 3, 0, 0, 0, 255)
        drawRect(xPosition + 2, yPosition + 14, p2, 1, 0, 255, 0, 255)
        drawRect(xPosition + 2, yPosition + 12.5, p1, 1, 255, 157, 0, 255)

        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
      case _ =>
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

  private def drawRect(x: Int, y: Double, width: Double, height: Double, red: Int, green: Int, blue: Int, alpha: Int): Unit = {
    val renderer: BufferBuilder = Tessellator.getInstance.getBuffer
    renderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
    renderer.pos((x + 0).toDouble, (y + 0).toDouble, 0.0D).color(red, green, blue, alpha).endVertex()
    renderer.pos((x + 0).toDouble, (y + height).toDouble, 0.0D).color(red, green, blue, alpha).endVertex()
    renderer.pos((x + width).toDouble, (y + height).toDouble, 0.0D).color(red, green, blue, alpha).endVertex()
    renderer.pos((x + width).toDouble, (y + 0).toDouble, 0.0D).color(red, green, blue, alpha).endVertex()
    Tessellator.getInstance.draw()
  }
}
