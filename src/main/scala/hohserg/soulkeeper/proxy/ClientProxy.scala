package hohserg.soulkeeper.proxy

import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.Main
import hohserg.soulkeeper.blocks.BlockInfuser.TileInfuser
import hohserg.soulkeeper.blocks.BlockRhOrb.TileRhOrb
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.network.ClientPacketHandler
import hohserg.soulkeeper.render._
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.{Render, RenderManager}
import net.minecraftforge.fml.client.registry.{ClientRegistry, IRenderFactory, RenderingRegistry}
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}

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

    Minecraft.getMinecraft.renderItem = new RenderItemWithCustomOverlay(Minecraft.getMinecraft.renderItem)

    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileInfuser], new TileInfuserRenderer)
    ClientRegistry.bindTileEntitySpecialRenderer(classOf[TileRhOrb], TileRhOrbRenderer)
  }

  override def postInit(event: FMLPostInitializationEvent): Unit = {
    super.postInit(event)
  }

}
