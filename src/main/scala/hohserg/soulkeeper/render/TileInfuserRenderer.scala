package hohserg.soulkeeper.render

import codechicken.lib.vec.Vector3
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.api.crafting.{DummyInfuserRecipe, InfuserRecipe, StepInfuserRecipe}
import hohserg.soulkeeper.blocks.BlockInfuser.TileInfuser
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.resources.I18n

class TileInfuserRenderer extends BaseTESR[TileInfuser] {
  override def render(te: TileInfuser, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    val input = te.inv.getStackInSlot(0)
    if (!input.isEmpty) {

      GlStateManager.pushMatrix()
      GlStateManager.translate(x + 0.5, y + 0.75 + 1d / 128, z + 0.5 - 2d / 16)
      GlStateManager.rotate(90, 1, 0, 0)
      GlStateManager.translate(0, 2d / 16, 0)
      GlStateManager.rotate(250, 0, 0, 1)
      GlStateManager.translate(0, -2d / 16, 0)
      Minecraft.getMinecraft.getRenderItem.renderItem(input, TransformType.GROUND)
      GlStateManager.popMatrix()

      InfuserRecipe.findRecipe(input).map {
        case r: DummyInfuserRecipe =>
          " 0/" + r.xp
        case r: StepInfuserRecipe =>
          val capa = CapabilityXPContainer(input)
          " " + capa.getXp + "/" + capa.getXpCapacity
      }.foreach { text =>
        val player = Minecraft.getMinecraft.player
        val dv = new Vector3(
          te.getPos.getX + 0.5 - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks),
          0,
          te.getPos.getZ + 0.5 - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks)
        ).normalize().multiply(0.4)
        val a = (-Math.toDegrees(Math.atan2(dv.x, dv.z)) + 180 + 180).toFloat
        drawString(I18n.format("soulkeeper.xp") + text, x + 0.5 + dv.x, y + 0.8, z + 0.5 + dv.z, a)
      }

    }
  }

}
