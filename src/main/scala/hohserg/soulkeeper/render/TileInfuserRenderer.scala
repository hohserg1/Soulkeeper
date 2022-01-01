package hohserg.soulkeeper.render

import codechicken.lib.vec.Vector3
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.api.crafting.{DummyInfuserRecipe, StepInfuserRecipe}
import hohserg.soulkeeper.blocks.BlockInfuser
import hohserg.soulkeeper.blocks.BlockInfuser.TileInfuser
import hohserg.soulkeeper.utils.ItemStackRepr
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{GlStateManager, Tessellator}
import net.minecraft.client.resources.I18n

class TileInfuserRenderer extends TileEntitySpecialRenderer[TileInfuser] {
  override def render(te: TileInfuser, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    val tool = te.inv.getStackInSlot(0)
    if (!tool.isEmpty) {

      GlStateManager.pushMatrix()
      //GlStateManager.translate(0, 0, +2d / 16)
      GlStateManager.translate(x + 0.5, y + 0.75 + 1d / 128, z + 0.5 - 2d / 16)
      GlStateManager.rotate(90, 1, 0, 0)
      GlStateManager.translate(0, 2d / 16, 0)
      GlStateManager.rotate(250, 0, 0, 1)
      GlStateManager.translate(0, -2d / 16, 0)
      Minecraft.getMinecraft.getRenderItem.renderItem(tool, TransformType.GROUND)
      GlStateManager.popMatrix()

      val text = BlockInfuser.recipeMap.get(ItemStackRepr.fromStack(tool)).map {
        case r: DummyInfuserRecipe =>
          I18n.format("soulkeeper.xp") + " 0/" + r.xp
        case r: StepInfuserRecipe =>
          val capa = CapabilityXPContainer(tool)
          I18n.format("soulkeeper.xp") + " " + capa.getXp + "/" + capa.getXpCapacity
      }.getOrElse("")
      //val a = Math.toRadians(rendererDispatcher.entityYaw + 90)
      //val dx = Math.cos(a)
      //val dz = Math.sin(a)

      val player = Minecraft.getMinecraft.player
      val dv = new Vector3(
        te.getPos.getX + 0.5 - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks),
        0,
        te.getPos.getZ + 0.5 - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks)
      ).normalize().multiply(0.4)
      val a = (-Math.toDegrees(Math.atan2(dv.x, dv.z)) + 180 + 180).toFloat
      drawString(text, x + 0.5 + dv.x, y + 0.8, z + 0.5 + dv.z, a)
    }
  }

  private def drawString(text: String, x: Double, y: Double, z: Double, yaw: Float): Unit = {

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
