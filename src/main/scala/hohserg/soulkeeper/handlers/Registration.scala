package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.blocks.BlockInfuser.TileInfuser
import hohserg.soulkeeper.blocks._
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.items.bottle.{ItemDustBottle, ItemEmptyBottle, ItemFilledBottle}
import hohserg.soulkeeper.items.fake.ItemEmptyBottleCork
import hohserg.soulkeeper.items.tools.{ItemRhAxe, ItemRhPickaxe, ItemRhShovel, ItemRhSword}
import hohserg.soulkeeper.items.{ItemDebugXPMeter, ItemRhinestoneDust, ItemTinyRhinestoneDust}
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.Entity
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.{EntityEntry, EntityEntryBuilder, GameRegistry}

@EventBusSubscriber(modid = Main.modid)
object Registration {

  lazy val tab = new CreativeTabs(Main.modid) {
    override def getTabIconItem: ItemStack = new ItemStack(BlockSoulkeeperPlant)
  }

  lazy val blocks = Seq(BlockDarkRhinestone, BlockSoulkeeperPlant, BlockDarkRhinestonePowder, BlockDarkRhinestoneStalactite, BlockInfuser)
  lazy val tools = Seq(ItemRhPickaxe, ItemRhAxe, ItemRhShovel, ItemRhSword)
  lazy val items =
    Seq(ItemTinyRhinestoneDust, ItemRhinestoneDust, ItemDebugXPMeter, ItemDustBottle, ItemEmptyBottle, ItemFilledBottle) ++
      blocks.map {
        case b: ItemBlockProvider => b.getItemBlock
        case b => new ItemBlock(b)
      } ++
      tools ++
      Seq(ItemEmptyBottleCork)


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

    GameRegistry.registerTileEntity(classOf[TileInfuser], new ResourceLocation(Main.modid, classOf[TileInfuser].getSimpleName.toLowerCase))
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

}
