package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.blocks.{BlockDarkRhinestoneStalactite, BlockRhOrb}
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = Main.modid)
object RemoveOnTouchOverlay {

  @SubscribeEvent
  def removeOnTouchOverlay(e: RenderBlockOverlayEvent): Unit = {
    if (e.getBlockForOverlay.getBlock == BlockDarkRhinestoneStalactite || e.getBlockForOverlay.getBlock == BlockRhOrb)
      e.setCanceled(true)
  }

}
