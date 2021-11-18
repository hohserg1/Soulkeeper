package hohserg.soulkeeper.proxy

import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.blocks.BlockInfuser.TileInfuser
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.network.ClientPacketHandler
import hohserg.soulkeeper.render.{CustomXPOrbRenderer, RenderItemWithCustomOverlay, RhToolModel, TileInfuserRenderer}
import hohserg.soulkeeper.{Main, XPUtils}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{GuiListWorldSelection, GuiMainMenu, GuiWorldSelection}
import net.minecraft.client.renderer.block.model.{IBakedModel, ModelResourceLocation}
import net.minecraft.client.renderer.entity.{Render, RenderManager}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event._
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.client.registry.{ClientRegistry, IRenderFactory, RenderingRegistry}
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import scala.util.Try

class ClientProxy extends CommonProxy {

  lazy val mc = Minecraft.getMinecraft

  override def preInit(event: FMLPreInitializationEvent): Unit = {
    super.preInit(event)
    PacketCustom.assignHandler(Main.modid, new ClientPacketHandler)
    RenderingRegistry.registerEntityRenderingHandler(classOf[CustomEntityXPOrb], new IRenderFactory[CustomEntityXPOrb] {
      override def createRenderFor(manager: RenderManager): Render[CustomEntityXPOrb] = {
        new CustomXPOrbRenderer(manager)
      }
    })
  }

  override def init(event: FMLInitializationEvent): Unit = {
    super.init(event)

    Minecraft.getMinecraft.renderItem = new RenderItemWithCustomOverlay(Minecraft.getMinecraft.renderItem)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileInfuser], new TileInfuserRenderer)
  }

  override def postInit(event: FMLPostInitializationEvent): Unit = {
    super.postInit(event)
  }

  @SubscribeEvent
  def onModelRegister(event: ModelRegistryEvent): Unit = {
    //blocks.map(Item.getItemFromBlock).foreach(i => ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(), "inventory")))
    items.foreach(i => ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(), "inventory")))

  }

  lazy val toolModels =
    tools.map {
      tool =>
        val name = tool.getRegistryName
        val key = new ModelResourceLocation(name, "inventory")
        val baseTextureName = name.getResourceDomain + ":items/" + name.getResourcePath
        val enchantedTextureName = baseTextureName + "_enchanted"
        val emptyTextureName = baseTextureName + "_empty"
        key -> (new RhToolModel(_: IBakedModel, enchantedTextureName, emptyTextureName), enchantedTextureName, emptyTextureName)
    }

  @SubscribeEvent
  def onToolModelRegister(event: ModelBakeEvent): Unit = {
    toolModels.foreach {
      case (key, (model, _, _)) =>
        event.getModelRegistry.putObject(key, model(event.getModelRegistry.getObject(key)))
    }
  }

  @SubscribeEvent
  def registerTextures(event: TextureStitchEvent): Unit = {
    toolModels.foreach {
      case (_, (_, enchantedTextureName, emptyTextureName)) =>
        event.getMap.registerSprite(new ResourceLocation(enchantedTextureName))
        event.getMap.registerSprite(new ResourceLocation(emptyTextureName))
    }
  }

  @SubscribeEvent
  def onOverlayRender(event: RenderGameOverlayEvent): Unit = {
    if (Main.debugMode)
      if (event.getType == RenderGameOverlayEvent.ElementType.AIR)
        mc.fontRenderer.drawString("player exp: " + XPUtils.getPlayerXP(mc.player), 10, 10, 0xff00ff)
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
