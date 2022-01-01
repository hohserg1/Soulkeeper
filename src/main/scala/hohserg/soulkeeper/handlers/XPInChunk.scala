package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.capability.chunk.ExpInChunkProvider
import hohserg.soulkeeper.{Main, XPUtils}
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = Main.modid)
object XPInChunk {

  @SubscribeEvent
  def onAttachCapaToChunk(event: AttachCapabilitiesEvent[Chunk]): Unit =
    event.addCapability(ExpInChunkProvider.name, new ExpInChunkProvider)

  @SubscribeEvent
  def onPlayerDropExp(event: LivingExperienceDropEvent): Unit =
    event.getEntity match {
      case player: EntityPlayer =>
        if (!player.world.getGameRules.getBoolean("keepInventory"))
          ExpInChunkProvider.getCapability(player.world, player.getPosition)
            .experience += (XPUtils.getPlayerXP(player) - EntityXPOrb.getXPSplit(event.getDroppedExperience))
      case _ =>
    }

}
