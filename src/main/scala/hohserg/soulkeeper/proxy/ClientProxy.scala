package hohserg.soulkeeper.proxy

import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.blocks.BlockInfuser.TileInfuser
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.items.bottle.{ItemEmptyBottle, ItemFilledBottle}
import hohserg.soulkeeper.network.ClientPacketHandler
import hohserg.soulkeeper.render._
import hohserg.soulkeeper.{Main, XPUtils}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{GuiListWorldSelection, GuiMainMenu, GuiWorldSelection}
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model.{BuiltInModel, IBakedModel, ItemOverrideList, ModelResourceLocation}
import net.minecraft.client.renderer.entity.{Render, RenderManager}
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.item.ItemStack
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
    ModelLoader.setCustomModelResourceLocation(ItemEmptyBottle, 0, new ModelResourceLocation(new ResourceLocation(Main.modid, "item_empty_bottle_cork"), "inventory"))
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
    val getModel = event.getModelRegistry.getObject _
    val setModel = event.getModelRegistry.putObject _

    toolModels.foreach {
      case (key, (model, _, _)) =>
        setModel(key, model(getModel(key)))
    }

    val bottleKey = new ModelResourceLocation(ItemEmptyBottle.getRegistryName(), "inventory")
    val bottleCorkKey = new ModelResourceLocation(new ResourceLocation(Main.modid, "item_empty_bottle_cork"), "inventory")
    val bottleModel = getModel(bottleKey)
    val corkModel = getModel(bottleCorkKey)
    setModel(bottleKey, new CombinedModel(corkModel, bottleModel))


    val contentKey = new ModelResourceLocation(ItemFilledBottle.getRegistryName(), "inventory")
    val contentModel = getModel(contentKey)
    val combinedBottleModel = getModel(bottleKey)

    setModel(contentKey, new BuiltInModel(bottleModel.getItemCameraTransforms, ItemOverrideList.NONE){
      override def handlePerspective(cameraTransformType: ItemCameraTransforms.TransformType): tuple.Pair[_ <: IBakedModel, Matrix4f] = {
        val matrix4f = bottleModel.handlePerspective(cameraTransformType).getRight
        Pair.of(this, matrix4f)
      }
    })

    val bottleStack = new ItemStack(ItemEmptyBottle)
    ItemFilledBottle.setTileEntityItemStackRenderer(new TileEntityItemStackRenderer {
      override def renderByItem(contentStack: ItemStack, partialTicks: Float): Unit = {
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.5, 0.5, 0.5)
        Minecraft.getMinecraft.getRenderItem.renderItem(contentStack, contentModel)
        Minecraft.getMinecraft.getRenderItem.renderItem(bottleStack, combinedBottleModel)
        GlStateManager.popMatrix()
      }
    })
  }

  @SubscribeEvent
  def registerTextures(event: TextureStitchEvent): Unit = {
    toolModels.foreach {
      case (_, (_, enchantedTextureName, emptyTextureName)) =>
        event.getMap.registerSprite(new ResourceLocation(enchantedTextureName))
        event.getMap.registerSprite(new ResourceLocation(emptyTextureName))
    }
    event.getMap.registerSprite(new ResourceLocation(Main.modid,"items/item_empty_bottle_cork"))
  }

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
