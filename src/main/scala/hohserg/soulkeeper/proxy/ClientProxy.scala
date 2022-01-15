package hohserg.soulkeeper.proxy

import java.util

import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.Main
import hohserg.soulkeeper.blocks.BlockInfuser.TileInfuser
import hohserg.soulkeeper.blocks.BlockRhOrb.TileRhOrb
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.network.ClientPacketHandler
import hohserg.soulkeeper.render._
import hohserg.soulkeeper.render.enchant.color._
import hohserg.soulkeeper.utils.ByteClassLoader
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity._
import net.minecraft.client.renderer.entity.layers.{LayerBipedArmor, LayerElytra}
import net.minecraft.client.resources.SimpleReloadableResourceManager
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.client.registry.{ClientRegistry, IRenderFactory, RenderingRegistry}
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}

import scala.collection.JavaConverters._

class ClientProxy extends CommonProxy {


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

    val originalRenderItem = Minecraft.getMinecraft.renderItem

    val newRenderItem = new RenderItemWithCustomEnchantColor(
      new RenderItemWithCustomOverlay(
        originalRenderItem
      )
    )

    Minecraft.getMinecraft.renderItem = newRenderItem
    Minecraft.getMinecraft.mcResourceManager match {
      case resourceManager: SimpleReloadableResourceManager =>
        resourceManager.reloadListeners.remove(originalRenderItem)
      case _ =>
    }
    Minecraft.getMinecraft.mcResourceManager.registerReloadListener(newRenderItem)
    Minecraft.getMinecraft.itemRenderer.itemRenderer = newRenderItem

    val classloader = new ByteClassLoader(Map(
      CustomLayerArmorFactory.bytes(),
      CustomLayerElytraFactory.bytes(),
      CustomEnchantColorRenderFactory.bytes()
    ))

    CustomEnchantColorRenderFactory.init(classloader)(newRenderItem)

    (Minecraft.getMinecraft.renderManager.entityRenderMap.asScala.values ++ Minecraft.getMinecraft.renderManager.getSkinMap.asScala.values).foreach {
      case render: RenderItemFrame => render.itemRenderer = newRenderItem
      case render: RenderSnowball[_] => render.itemRenderer = newRenderItem
      case render: RenderEntityItem => render.itemRenderer = newRenderItem
      case render: RenderLivingBase[EntityLivingBase] =>
        render.layerRenderers = new util.ArrayList(render.layerRenderers.asScala.map {
          case layer: LayerBipedArmor => CustomLayerArmorFactory.create(classloader)(render, layer)
          case layer: LayerElytra => CustomLayerElytraFactory.create(classloader)(render, layer)
          case layer => layer
        }.asJava)
      case _ =>
    }


    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileInfuser], new TileInfuserRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileRhOrb], TileRhOrbRenderer)
  }

  override def postInit(event: FMLPostInitializationEvent): Unit = {
    super.postInit(event)
  }

}
