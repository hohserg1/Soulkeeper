package hohserg.soulkeeper.proxy

import java.util.concurrent.Callable

import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.blocks.{BlockDarkRhinestone, BlockDarkRhinestonePowder, BlockDarkRhinestoneStalactite, BlockSoulkeeperPlant}
import hohserg.soulkeeper.capability.ExpInChunkProvider.getCapability
import hohserg.soulkeeper.capability.{ExpInChunk, ExpInChunkProvider, ExpInChunkStorage}
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.items.{ItemDebugXPMeter, ItemRhinestoneDust, ItemTinyRhinestoneDust}
import hohserg.soulkeeper.network.ServerPacketHandler
import hohserg.soulkeeper.worldgen.{SoulkeeperPlantGenerator, StalactiteGenerator}
import hohserg.soulkeeper.{Main, XPUtils}
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.entity.{EntityEvent, EntityJoinWorldEvent}
import net.minecraftforge.event.{AttachCapabilitiesEvent, RegistryEvent}
import net.minecraftforge.fml.common.event.{FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.{EntityEntry, EntityEntryBuilder, GameRegistry}

class CommonProxy {

  def preInit(event: FMLPreInitializationEvent): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
    CapabilityManager.INSTANCE.register(classOf[ExpInChunk], new ExpInChunkStorage, new Callable[ExpInChunk] {
      override def call(): ExpInChunk = new ExpInChunk
    })
  }

  def init(event: FMLPreInitializationEvent): Unit = {
    PacketCustom.assignHandler(Main.modid, new ServerPacketHandler)
    GameRegistry.registerWorldGenerator(SoulkeeperPlantGenerator, 0)
    GameRegistry.registerWorldGenerator(StalactiteGenerator, 0)

  }

  def postInit(event: FMLPostInitializationEvent): Unit = {

  }

  lazy val tab = new CreativeTabs(Main.modid) {
    override def getTabIconItem: ItemStack = new ItemStack(BlockSoulkeeperPlant)
  }

  lazy val blocks = Seq(BlockDarkRhinestone, BlockSoulkeeperPlant, BlockDarkRhinestonePowder, BlockDarkRhinestoneStalactite)
  lazy val items = Seq(ItemTinyRhinestoneDust, ItemRhinestoneDust, ItemDebugXPMeter) ++ blocks.map(new ItemBlock(_))

  def toId(r: String): String = {
    val r1 = r.flatMap(c => if (c.isUpper) "_" + c else "" + c)
    if (r1.startsWith("_")) r1.substring(1)
    else r1
  }.toLowerCase

  @SubscribeEvent
  def onRegisterBlocks(event: RegistryEvent.Register[Block]): Unit = {
    def register(block: Block): Unit = {
      val name = toId(block.getClass.getSimpleName).init
      event.getRegistry.register(block.setRegistryName(name).setUnlocalizedName(name).setCreativeTab(tab))
    }

    blocks.foreach(register)
  }

  @SubscribeEvent
  def onRegisterItems(event: RegistryEvent.Register[Item]): Unit = {
    def register(item: Item): Unit =
      event.getRegistry.register(
        item match {
          case itemBlock: ItemBlock =>
            itemBlock.setRegistryName(itemBlock.getBlock.getRegistryName).setUnlocalizedName(itemBlock.getBlock.getUnlocalizedName)
          case _ =>
            val name = toId(item.getClass.getSimpleName).init
            item.setRegistryName(name).setUnlocalizedName(name).setCreativeTab(tab)
        }
      )


    items.foreach(register)
  }

  @SubscribeEvent
  def onRegisterEntities(event: RegistryEvent.Register[EntityEntry]): Unit = {
    var id = -1

    def createEntityEntry(clazz: Class[_ <: Entity], range: Int, updateFrequency: Int, sendVelocityUpdate: Boolean): EntityEntry = {
      val name: String = clazz.getSimpleName.toLowerCase
      id += 1
      EntityEntryBuilder
        .create()
        .entity(clazz)
        .name(name)
        .id(toId(name), id)
        .tracker(range, updateFrequency, sendVelocityUpdate)
        .build()
    }

    event.getRegistry.register(createEntityEntry(classOf[CustomEntityXPOrb], 160, 20, sendVelocityUpdate = true))
  }

  @SubscribeEvent
  def onAttachCapaToChunk(event: AttachCapabilitiesEvent[Chunk]): Unit =
    event.addCapability(ExpInChunkProvider.name, new ExpInChunkProvider)

  ///summon minecraft:xp_orb 0 5 -3 {Value:100}
  @SubscribeEvent
  def onExpOrbWorldEnter(event: EntityJoinWorldEvent): Unit = {
    val world = event.getWorld
    if (!event.getWorld.isRemote)
      event.getEntity match {
        case _: CustomEntityXPOrb =>
        case orb: EntityXPOrb if !orb.isInstanceOf[CustomEntityXPOrb] =>
          event.setCanceled(true)
          event.getWorld.spawnEntity(new CustomEntityXPOrb(orb))
        case _ =>
      }
  }

  @SubscribeEvent
  def onPlayerDropExp(event: LivingExperienceDropEvent): Unit =
    event.getEntity match {
      case player: EntityPlayer =>
        getCapability(player.world, player.getPosition)
          .experience += (XPUtils.getPlayerXP(player) - EntityXPOrb.getXPSplit(event.getDroppedExperience))
      case _ =>
    }


  @SubscribeEvent
  def onEntityConstructing(event: EntityEvent.EntityConstructing) {
    event.getEntity match {
      case _ =>
    }
  }

  @SubscribeEvent
  def onStickClick(event: PlayerInteractEvent.RightClickBlock): Unit = {
    //println(event.getWorld.getBlockState(event.getPos.up))
  }

}
