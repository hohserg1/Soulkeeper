package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.{Main, XPUtils}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui._
import net.minecraftforge.client.event.{GuiOpenEvent, RenderGameOverlayEvent}
import net.minecraftforge.fml.client.{FMLClientHandler, GuiConfirmation}
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.{ReflectionHelper, Side, SideOnly}

import scala.util.Try

@EventBusSubscriber(modid = Main.modid)
object Debug {
  lazy val mc = Minecraft.getMinecraft

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  def onOverlayRender(event: RenderGameOverlayEvent): Unit = {
    if (Main.debugMode)
      if (event.getType == RenderGameOverlayEvent.ElementType.AIR)
        mc.fontRenderer.drawString("player exp: " + XPUtils.getPlayerXP(mc.player), 10, 10, 0xff00ff)
  }

  private var alreadyEnteredInWorldAutomaticaly = false
  private var mainMenu: GuiMainMenu = _

  @SideOnly(Side.CLIENT)
  @SubscribeEvent
  @throws[IllegalAccessException]
  def loadLastWorld(event: GuiOpenEvent): Unit =
    if (Main.debugMode)
      if (!alreadyEnteredInWorldAutomaticaly) {
        val mc = Minecraft.getMinecraft
        event.getGui match {
          case menu: GuiMainMenu =>
            mainMenu = menu
            mc.displayGuiScreen(new GuiWorldSelection(menu))
          case selection: GuiWorldSelection =>
            println(Try(
              new GuiListWorldSelection(selection, mc, 100, 100, 32, 100 - 64, 36)
                .getListEntry(0)
                .joinWorld()
            ))
          case _: GuiConfirmation =>
            alreadyEnteredInWorldAutomaticaly = true
            ReflectionHelper.findMethod(classOf[GuiConfirmation], "actionPerformed", null, classOf[GuiButton]).invoke(event.getGui, new GuiButton(1, 0, 0, ""))
            FMLClientHandler.instance.showGuiScreen(mainMenu)
          case _: GuiIngameMenu => alreadyEnteredInWorldAutomaticaly = true
          case _ =>
        }
      }

}
