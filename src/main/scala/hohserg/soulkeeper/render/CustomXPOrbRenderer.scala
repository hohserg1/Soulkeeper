package hohserg.soulkeeper.render

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.{Render, RenderManager}
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.util.ResourceLocation

class CustomXPOrbRenderer(rm: RenderManager) extends Render[CustomEntityXPOrb](rm) {
  lazy val vanillaRenderer = Minecraft.getMinecraft.getRenderManager.getEntityClassRenderObject[EntityXPOrb](classOf[EntityXPOrb])

  override def getEntityTexture(entity: CustomEntityXPOrb): ResourceLocation = new ResourceLocation("textures/entity/experience_orb.png")

  override def doRender(entity: CustomEntityXPOrb, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float): Unit = {
    vanillaRenderer.doRender(entity, x, y, z, entityYaw, partialTicks)
    if (Main.debugMode)
      renderLivingLabel(entity, "" + entity.xpValue, x, y, z, 100)
  }
}
