package hohserg.soulkeeper.potions

import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.{Configuration, Main, XPUtils}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.potion.{Potion, PotionEffect}
import net.minecraft.util.ResourceLocation

object EffectXPLeak extends Potion(true, 0xff065C75) {
  setPotionName("effect.xp_leak")
  setRegistryName("xp_leak")

  override def performEffect(entityLivingBaseIn: EntityLivingBase, amplifier: Int): Unit = {
    entityLivingBaseIn match {
      case player: EntityPlayerMP =>
        val xp = XPUtils.getPlayerXP(player)
        if (xp > 0) {
          val leaked = math.min(xp, Configuration.xpLeakPerEffectLevel * (amplifier + 1))
          XPUtils.setPlayerXP(player, xp - leaked)
          val orb = new CustomEntityXPOrb(leaked, player.world, player.posX, player.posY, player.posZ)
          orb.consumerBlacklist = Set(player.getName)
          player.world.spawnEntity(orb)
        }
      case _ =>
    }
  }

  override def isReady(duration: Int, amplifier: Int): Boolean = duration % 10 == 0

  val icon = new ResourceLocation(Main.modid, "textures/potions/xp_leak.png")

  override def renderInventoryEffect(x: Int, y: Int, effect: PotionEffect, mc: Minecraft): Unit = {
    if (mc.currentScreen != null) {
      mc.getTextureManager.bindTexture(icon)
      Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18, 18)
    }
  }

  override def renderHUDEffect(x: Int, y: Int, effect: PotionEffect, mc: Minecraft, alpha: Float): Unit = {
    mc.getTextureManager.bindTexture(icon)
    Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18, 18)
  }

}
