package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.blocks.BlockDarkRhinestonePowder
import hohserg.soulkeeper.handlers.Registration._
import hohserg.soulkeeper.items.bottle.{ItemEmptyBottle, ItemFilledBottle}
import hohserg.soulkeeper.render.{BottleModel, RhToolModel}
import javax.vecmath.Matrix4f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.block.model._
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.{ModelBakeEvent, ModelRegistryEvent, TextureStitchEvent}
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.commons.lang3.tuple
import org.apache.commons.lang3.tuple.Pair

@EventBusSubscriber(modid = Main.modid)
object ModelRegistration {

  @SubscribeEvent
  def onModelRegister(event: ModelRegistryEvent): Unit = {
    items.foreach(i => ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation(i.getRegistryName(), "inventory")))

    val powderItemBlock = Item.getItemFromBlock(BlockDarkRhinestonePowder)
    for (i <- 1 to 14)
      ModelLoader.setCustomModelResourceLocation(powderItemBlock, i, new ModelResourceLocation(powderItemBlock.getRegistryName(), "inventory"))
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
    setModel(bottleKey, new BottleModel(corkModel, bottleModel))


    val contentKey = new ModelResourceLocation(ItemFilledBottle.getRegistryName(), "inventory")
    val contentModel = getModel(contentKey)
    val combinedBottleModel = getModel(bottleKey)

    setModel(contentKey, new BuiltInModel(combinedBottleModel.getItemCameraTransforms, ItemOverrideList.NONE) {
      override def handlePerspective(cameraTransformType: ItemCameraTransforms.TransformType): tuple.Pair[_ <: IBakedModel, Matrix4f] = {
        val matrix4f = combinedBottleModel.handlePerspective(cameraTransformType).getRight
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
  }

}
