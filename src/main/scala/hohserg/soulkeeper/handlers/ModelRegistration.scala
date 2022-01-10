package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.blocks.{BlockDarkRhinestonePowder, BlockRhOrb}
import hohserg.soulkeeper.handlers.Registration._
import hohserg.soulkeeper.items.ItemRhShield
import hohserg.soulkeeper.items.bottle.{ItemEmptyBottle, ItemFilledBottle}
import hohserg.soulkeeper.render._
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model._
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{GlStateManager, RenderHelper}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.{ModelBakeEvent, ModelRegistryEvent, TextureStitchEvent}
import net.minecraftforge.client.model.{IModel, ModelLoader}
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = Main.modid)
object ModelRegistration {

  @SubscribeEvent
  def onModelRegister(event: ModelRegistryEvent): Unit = {
    items.foreach(i => ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(), "inventory")))

    val powderItemBlock = Item.getItemFromBlock(BlockDarkRhinestonePowder)
    for (i <- 1 to 15)
      ModelLoader.setCustomModelResourceLocation(powderItemBlock, i, new ModelResourceLocation(powderItemBlock.getRegistryName(), "inventory"))
  }

  val loadModel: ResourceLocation => IModel = {
    val VanillaLoaderClass = classOf[net.minecraftforge.client.model.ModelLoader].getDeclaredClasses.filter(cl => cl.getSimpleName == "VanillaLoader").head
    val instanceField = VanillaLoaderClass.getDeclaredField("INSTANCE")
    instanceField.setAccessible(true)
    val instance = instanceField.get(null)
    val loadModel = VanillaLoaderClass.getDeclaredMethod("loadModel", classOf[ResourceLocation])
    loadModel.setAccessible(true)
    loadModel.invoke(instance, _).asInstanceOf[IModel]
  }

  def bakeModel(model: IModel): IBakedModel = model.bake(model.getDefaultState, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter())

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

  lazy val shieldEnchantedModel = loadModel(new ResourceLocation(Main.modid, "models/item/shield_enchanted"))
  lazy val shieldHandleModel = loadModel(new ResourceLocation(Main.modid, "models/item/shield_handle"))
  lazy val shieldEmptyModel = loadModel(new ResourceLocation(Main.modid, "models/item/shield_empty"))

  lazy val shieldEnchantedBakedModel = bakeModel(shieldEnchantedModel)
  lazy val shieldHandleBakedModel = bakeModel(shieldHandleModel)
  lazy val shieldEmptyBakedModel = bakeModel(shieldEmptyModel)

  lazy val bottleCorkModel = loadModel(new ResourceLocation(Main.modid, "models/item/item_empty_bottle_cork"))
  lazy val bottleCorkBakedModel = bakeModel(bottleCorkModel)

  @SubscribeEvent
  def onToolModelRegister(event: ModelBakeEvent): Unit = {
    val getModel = event.getModelRegistry.getObject _
    val setModel = event.getModelRegistry.putObject _

    def wrapModel(key: ModelResourceLocation, wrapper: IBakedModel => IBakedModel): Unit =
      setModel(key, wrapper(getModel(key)))


    toolModels.foreach {
      case (key, (model, _, _)) =>
        setModel(key, model(getModel(key)))
    }

    val bottleKey = new ModelResourceLocation(ItemEmptyBottle.getRegistryName(), "inventory")
    val bottleModel = getModel(bottleKey)
    setModel(bottleKey, new BottleModel(bottleCorkBakedModel, bottleModel))


    val contentKey = new ModelResourceLocation(ItemFilledBottle.getRegistryName(), "inventory")
    val contentModel = getModel(contentKey)
    val combinedBottleModel = getModel(bottleKey)

    setModel(contentKey, new BuiltinModelDelegate(combinedBottleModel))

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

    wrapModel(new ModelResourceLocation(ItemRhShield.getRegistryName, "inventory"), new RhShieldRenderer.ShieldBakedModel(_))
    wrapModel(new ModelResourceLocation(new ResourceLocation(Main.modid, "item/item_rh_shield_blocking"), "inventory"), new RhShieldRenderer.ShieldBakedModel(_))
    ItemRhShield.setTileEntityItemStackRenderer(RhShieldRenderer)

    val orbModelKey = new ModelResourceLocation(BlockRhOrb.getRegistryName, "inventory")
    val orbModel = getModel(orbModelKey)
    setModel(orbModelKey, new BuiltinModelDelegate(orbModel))
    Item.getItemFromBlock(BlockRhOrb).setTileEntityItemStackRenderer(new TileEntityItemStackRenderer {
      override def renderByItem(stack: ItemStack, partialTicks: Float): Unit = {
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()

        GlStateManager.pushMatrix()

        RenderHelper.enableGUIStandardItemLighting()
        TileRhOrbRenderer.drawFluidLevel(CapabilityXPContainer(stack).getXp, 0, 0, 0, partialTicks)

        GlStateManager.translate(0.5, 0.5, 0.5)
        RenderHelper.enableStandardItemLighting()
        Minecraft.getMinecraft.getTextureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        Minecraft.getMinecraft.getRenderItem.renderItem(stack, orbModel)

        GlStateManager.popMatrix()
      }
    })
  }


  import collection.JavaConverters._


  @SubscribeEvent
  def registerTextures(event: TextureStitchEvent.Pre): Unit = {
    toolModels.foreach {
      case (_, (_, enchantedTextureName, emptyTextureName)) =>
        event.getMap.registerSprite(new ResourceLocation(enchantedTextureName))
        event.getMap.registerSprite(new ResourceLocation(emptyTextureName))
    }

    Seq(
      shieldEnchantedModel,
      shieldHandleModel,
      shieldEmptyModel,
      bottleCorkModel
    ).foreach(_.getTextures.asScala.foreach(event.getMap.registerSprite))

    RhShieldRenderer.preparedShieldPatternTextures(event.getMap)
  }

}
