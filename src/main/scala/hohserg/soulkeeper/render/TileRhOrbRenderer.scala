package hohserg.soulkeeper.render

import codechicken.lib.vec.Vector3
import hohserg.soulkeeper.blocks.BlockRhOrb.TileRhOrb
import hohserg.soulkeeper.{Configuration, Main}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{BufferBuilder, GlStateManager, Tessellator}
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

object TileRhOrbRenderer extends BaseTESR[TileRhOrb] {

  override def render(te: TileRhOrb, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    drawLabel(te, x, y, z, partialTicks)
    drawFluidLevel(te.xp, x, y, z, partialTicks)
  }

  val texture1 = new ResourceLocation(Main.modid, "textures/blocks/rh_orb/fluid1.png")
  val texture2 = new ResourceLocation(Main.modid, "textures/blocks/rh_orb/fluid2.png")
  val texture3 = new ResourceLocation(Main.modid, "textures/blocks/rh_orb/fluid3.png")

  def drawFluidLevel(xp: Int, x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    if (xp > 0) {
      val pixel = 1d / 16
      val fluidLevel = xp.toDouble / Configuration.rhinestoneOrbCapacity
      val fullHeight = 10
      val startY = 2
      val texture2Y = (4, 10)

      val fluidY = startY + fullHeight * fluidLevel

      val texture = if (fluidY > texture2Y._1 && fluidY <= texture2Y._2) texture2 else texture1

      val x1 = 3 * pixel
      val z1 = 3 * pixel
      val x2 = (16 - 3) * pixel
      val z2 = (16 - 3) * pixel

      val fy = fluidY * pixel


      def drawTop(buffer: BufferBuilder): Unit = {
        Minecraft.getMinecraft.getTextureManager.bindTexture(texture)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)

        buffer.pos(x1, fy, z1).tex(0, 0).endVertex()
        buffer.pos(x1, fy, z2).tex(1, 0).endVertex()
        buffer.pos(x2, fy, z2).tex(1, 1).endVertex()
        buffer.pos(x2, fy, z1).tex(0, 1).endVertex()

        Tessellator.getInstance().draw()
      }

      def drawSides(buffer: BufferBuilder): Unit = {
        Minecraft.getMinecraft.getTextureManager.bindTexture(texture1)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)

        buffer.pos(x1, pixel * startY, z1).tex(0, 0).endVertex()
        buffer.pos(x1, pixel * startY, z2).tex(1, 0).endVertex()
        buffer.pos(x1, fy, z2).tex(1, fluidLevel).endVertex()
        buffer.pos(x1, fy, z1).tex(0, fluidLevel).endVertex()

        buffer.pos(x2, fy, z1).tex(0, fluidLevel).endVertex()
        buffer.pos(x2, fy, z2).tex(1, fluidLevel).endVertex()
        buffer.pos(x2, pixel * startY, z2).tex(1, 0).endVertex()
        buffer.pos(x2, pixel * startY, z1).tex(0, 0).endVertex()

        buffer.pos(x1, fy, z1).tex(0, fluidLevel).endVertex()
        buffer.pos(x2, fy, z1).tex(1, fluidLevel).endVertex()
        buffer.pos(x2, pixel * startY, z1).tex(1, 0).endVertex()
        buffer.pos(x1, pixel * startY, z1).tex(0, 0).endVertex()

        buffer.pos(x1, pixel * startY, z2).tex(0, 0).endVertex()
        buffer.pos(x2, pixel * startY, z2).tex(1, 0).endVertex()
        buffer.pos(x2, fy, z2).tex(1, fluidLevel).endVertex()
        buffer.pos(x1, fy, z2).tex(0, fluidLevel).endVertex()

        Tessellator.getInstance().draw()
      }

      def drawCornerTop(buffer: BufferBuilder): Unit = {
        if (fluidY > texture2Y._2) {
          Minecraft.getMinecraft.getTextureManager.bindTexture(texture3)
          buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)

          buffer.pos(x1, texture2Y._2 * pixel, z1).tex(0, 0).endVertex()
          buffer.pos(x1, texture2Y._2 * pixel, z2).tex(1, 0).endVertex()
          buffer.pos(x2, texture2Y._2 * pixel, z2).tex(1, 1).endVertex()
          buffer.pos(x2, texture2Y._2 * pixel, z1).tex(0, 1).endVertex()

          Tessellator.getInstance().draw()
        }
      }

      def drawCornerSides(buffer: BufferBuilder): Unit = {
        Minecraft.getMinecraft.getTextureManager.bindTexture(texture3)

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)

        buffer.pos(x1 + pixel * 2, pixel * startY, z1).tex(0, 0).endVertex()
        buffer.pos(x1 + pixel * 2, pixel * startY, z2).tex(1, 0).endVertex()
        buffer.pos(x1 + pixel * 2, fy, z2).tex(1, fluidLevel).endVertex()
        buffer.pos(x1 + pixel * 2, fy, z1).tex(0, fluidLevel).endVertex()

        buffer.pos(x2 - pixel * 2, fy, z1).tex(0, fluidLevel).endVertex()
        buffer.pos(x2 - pixel * 2, fy, z2).tex(1, fluidLevel).endVertex()
        buffer.pos(x2 - pixel * 2, pixel * startY, z2).tex(1, 0).endVertex()
        buffer.pos(x2 - pixel * 2, pixel * startY, z1).tex(0, 0).endVertex()


        buffer.pos(x1, fy, z1 + pixel * 2).tex(0, fluidLevel).endVertex()
        buffer.pos(x2, fy, z1 + pixel * 2).tex(1, fluidLevel).endVertex()
        buffer.pos(x2, pixel * startY, z1 + pixel * 2).tex(1, 0).endVertex()
        buffer.pos(x1, pixel * startY, z1 + pixel * 2).tex(0, 0).endVertex()

        buffer.pos(x1, pixel * startY, z2 - pixel * 2).tex(0, 0).endVertex()
        buffer.pos(x2, pixel * startY, z2 - pixel * 2).tex(1, 0).endVertex()
        buffer.pos(x2, fy, z2 - pixel * 2).tex(1, fluidLevel).endVertex()
        buffer.pos(x1, fy, z2 - pixel * 2).tex(0, fluidLevel).endVertex()

        Tessellator.getInstance().draw()

      }

      GlStateManager.enableAlpha()
      GlStateManager.enableBlend()
      GlStateManager.enableLighting()
      GlStateManager.pushMatrix()

      GlStateManager.translate(x, y, z)

      val buffer = Tessellator.getInstance().getBuffer

      drawTop(buffer)
      drawSides(buffer)
      drawCornerTop(buffer)
      drawCornerSides(buffer)

      GlStateManager.popMatrix()
    }
  }

  private def drawLabel(te: TileRhOrb, x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    val text = te.xp + "/" + Configuration.rhinestoneOrbCapacity

    val player = Minecraft.getMinecraft.player
    val dv = new Vector3(
      te.getPos.getX + 0.5 - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks),
      0,
      te.getPos.getZ + 0.5 - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks)
    ).normalize().multiply(0.4)
    val a = (-Math.toDegrees(Math.atan2(dv.x, dv.z)) + 180 + 180).toFloat
    drawString(text, x + 0.5 + dv.x, y + 0.9, z + 0.5 + dv.z, a)
  }
}
