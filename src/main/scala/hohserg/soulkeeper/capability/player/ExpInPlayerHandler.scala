package hohserg.soulkeeper.capability.player

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.api.events.ChangePlayerXPEvent
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent

@EventBusSubscriber(modid = Main.modid)
object ExpInPlayerHandler {

  @SubscribeEvent
  def onAttachCapaToPlayer(event: AttachCapabilitiesEvent[Entity]): Unit =
    event.getObject match {
      case player: EntityPlayer =>
        event.addCapability(ExpInPlayerProvider.name, new ExpInPlayerProvider(player))
      case _ =>
    }

  def calculateTotal(experienceLevel: Int, experiencePartial: Float): Int = ???

  def calculateLevel(experienceTotal: Int): (Int, Float) = ???

  //@SubscribeEvent
  def updatePlayer(e: PlayerTickEvent): Unit = {
    val player = e.player
    val prev = ExpInPlayerProvider.getCapability(player)
    if (prev.experienceLevel != player.experienceLevel) {
      player.experienceTotal = calculateTotal(player.experienceLevel, player.experience)
      fireEventAndSyncPrev(player, prev)
    } else if (prev.experienceTotal != player.experienceTotal) {
      val (level, partial) = calculateLevel(player.experienceTotal)
      player.experienceLevel = level
      player.experience = partial
      fireEventAndSyncPrev(player, prev)
    }
  }

  private def fireEventAndSyncPrev(player: EntityPlayer, prev: ExpInPlayer): Unit = {
    val event = ChangePlayerXPEvent(
      player,
      prev.experienceLevel, prev.experienceTotal, prev.experience,
      player.experienceLevel, player.experienceTotal, player.experience
    )
    MinecraftForge.EVENT_BUS.post(event)
    prev.syncWithPlayer(player)
  }
}
