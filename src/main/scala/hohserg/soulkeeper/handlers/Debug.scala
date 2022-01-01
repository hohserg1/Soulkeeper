package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.{Main, XPUtils}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{GuiListWorldSelection, GuiMainMenu, GuiWorldSelection}
import net.minecraftforge.client.event.{GuiOpenEvent, RenderGameOverlayEvent}
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import scala.util.Try

@EventBusSubscriber(modid = Main.modid)
object Debug {
  lazy val mc = Minecraft.getMinecraft

  @SubscribeEvent
  def onOverlayRender(event: RenderGameOverlayEvent): Unit = {
    if (Main.debugMode)
      if (event.getType == RenderGameOverlayEvent.ElementType.AIR)
        mc.fontRenderer.drawString("player exp: " + XPUtils.getPlayerXP(mc.player), 10, 10, 0xff00ff)
  }

  @SubscribeEvent
  def onMainMenu(event: GuiOpenEvent): Unit =
    if (Main.debugMode)
      event.getGui match {
        case guiMainMenu: GuiMainMenu => mc.displayGuiScreen(new GuiWorldSelection(guiMainMenu))
        case selection: GuiWorldSelection =>
          val guiListWorldSelection = new GuiListWorldSelection(selection, mc, 100, 100, 32, 100 - 64, 36)
          Try(guiListWorldSelection.getListEntry(0).joinWorld())
        case _ =>
      }
}
