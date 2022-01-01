package hohserg.soulkeeper.proxy

import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.Main
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.capability.chunk.{ExpInChunk, ExpInChunkStorage}
import hohserg.soulkeeper.capability.player.{ExpInPlayer, ExpInPlayerStorage}
import hohserg.soulkeeper.capability.tile.PrevLootTable
import hohserg.soulkeeper.capability.{DummyFactory, DummyStorage}
import hohserg.soulkeeper.network.ServerPacketHandler
import hohserg.soulkeeper.worldgen.{SoulkeeperPlantGenerator, StalactiteGenerator}
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.registry.GameRegistry

class CommonProxy {

  def preInit(event: FMLPreInitializationEvent): Unit = {
    PacketCustom.assignHandler(Main.modid, new ServerPacketHandler)
    CapabilityManager.INSTANCE.register(classOf[ExpInChunk], new ExpInChunkStorage, DummyFactory(() => new ExpInChunk))
    CapabilityManager.INSTANCE.register(classOf[ExpInPlayer], new ExpInPlayerStorage, DummyFactory(() => new ExpInPlayer))
    CapabilityManager.INSTANCE.register(classOf[PrevLootTable], new DummyStorage[PrevLootTable], DummyFactory(() => new PrevLootTable))
    CapabilityManager.INSTANCE.register(classOf[CapabilityXPContainer], new DummyStorage[CapabilityXPContainer], DummyFactory(() => null))
  }

  def init(event: FMLInitializationEvent): Unit = {
    GameRegistry.registerWorldGenerator(SoulkeeperPlantGenerator, 0)
    GameRegistry.registerWorldGenerator(StalactiteGenerator, 0)

  }

  def postInit(event: FMLPostInitializationEvent): Unit = {

  }

}
