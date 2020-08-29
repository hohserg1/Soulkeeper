package hohserg.soulkeeper.proxy

import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.Main
import hohserg.soulkeeper.network.ClientPacketHandler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{GuiListWorldSelection, GuiMainMenu, GuiWorldSelection}
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.Item
import net.minecraftforge.client.event.{GuiOpenEvent, ModelRegistryEvent, RenderGameOverlayEvent}
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.event.{FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import scala.util.Try

class ClientProxy extends CommonProxy {

  lazy val mc = Minecraft.getMinecraft

  override def preInit(event: FMLPreInitializationEvent): Unit = {
    super.preInit(event)
  }

  override def init(event: FMLPreInitializationEvent): Unit = {
    super.init(event)
    PacketCustom.assignHandler(Main.modid, new ClientPacketHandler)
  }

  override def postInit(event: FMLPostInitializationEvent): Unit = {
    super.postInit(event)
  }

  @SubscribeEvent
  def onModelRegister(event: ModelRegistryEvent): Unit = {
    blocks.map(Item.getItemFromBlock).foreach(i => ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(), "inventory")))
    items.foreach(i => ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(), "inventory")))

  }

  @SubscribeEvent
  def onOverlayRender(event: RenderGameOverlayEvent): Unit = {
    if (event.getType == RenderGameOverlayEvent.ElementType.AIR)
    //mc.fontRenderer.drawString("chunk exp: " + mc.world.getChunkFromBlockCoords(mc.player.getPosition).getCapability(Capabilities.expInChunk, EnumFacing.UP).experience, 10, 10, 0xff00ff)
      mc.fontRenderer.drawString("player exp: " + mc.player.experienceTotal, 10, 10, 0xff00ff)

  }

  @SubscribeEvent
  def onMainMenu(event: GuiOpenEvent): Unit =
    event.getGui match {
      case guiMainMenu: GuiMainMenu => mc.displayGuiScreen(new GuiWorldSelection(guiMainMenu))
      case selection: GuiWorldSelection =>
        val guiListWorldSelection = new GuiListWorldSelection(selection, mc, 100, 100, 32, 100 - 64, 36)
        Try(guiListWorldSelection.getListEntry(0).joinWorld())
      case _ =>
    }
}
