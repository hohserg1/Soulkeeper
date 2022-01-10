package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.blocks.BlockInfuser.TileInfuser
import hohserg.soulkeeper.blocks.BlockRhOrb.TileRhOrb
import hohserg.soulkeeper.blocks._
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.items.bottle.{ItemDustBottle, ItemEmptyBottle, ItemFilledBottle}
import hohserg.soulkeeper.items.tools.{ItemRhAxe, ItemRhPickaxe, ItemRhShovel, ItemRhSword}
import hohserg.soulkeeper.items.{ItemDebugXPMeter, ItemRhShield, ItemRhinestoneDust, ItemTinyRhinestoneDust}
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.Entity
import net.minecraft.init.Items
import net.minecraft.item.crafting.{IRecipe, Ingredient, ShapedRecipes}
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.tileentity.BannerPattern
import net.minecraft.util.{NonNullList, ResourceLocation}
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.{EntityEntry, EntityEntryBuilder, GameRegistry}

@EventBusSubscriber(modid = Main.modid)
object Registration {

  lazy val tab = new CreativeTabs(Main.modid) {
    override def getTabIconItem: ItemStack = new ItemStack(BlockSoulkeeperPlant)
  }

  lazy val blocks = Seq(BlockSoulkeeperPlant, BlockDarkRhinestonePowder, BlockDarkRhinestoneStalactite, BlockInfuser, BlockRhOrb, BlockRhPowderOrb)
  lazy val tools = Seq(ItemRhPickaxe, ItemRhAxe, ItemRhShovel, ItemRhSword)
  lazy val items =
    Seq(ItemTinyRhinestoneDust, ItemRhinestoneDust, ItemDebugXPMeter, ItemDustBottle, ItemEmptyBottle, ItemFilledBottle, ItemRhShield) ++
      blocks.map {
        case b: ItemBlockProvider => b.getItemBlock
        case b => new ItemBlock(b)
      } ++
      tools


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
    GameRegistry.registerTileEntity(classOf[TileRhOrb], new ResourceLocation(Main.modid, classOf[TileRhOrb].getSimpleName.toLowerCase))
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
  def onRegisterRecipes(event: RegistryEvent.Register[IRecipe]): Unit = {
    event.getRegistry.registerAll(
      BannerPattern.values()
        .filter(pattern => pattern.hasPatternItem)
        .map(pattern => new ShapedRecipes(ItemRhShield.getRegistryName.toString, 3, 3, {
          val r = Ingredient.fromItem(ItemRhinestoneDust)
          val i = Ingredient.fromItem(Items.IRON_INGOT)
          val e = Ingredient.EMPTY
          val c = Ingredient.fromStacks(pattern.getPatternItem)

          NonNullList.from(e,
            r, c, r,
            r, i, r,
            e, r, e
          )
        }, {
          val result = new ItemStack(ItemRhShield)
          result.getOrCreateSubCompound("shieldData").setString("pattern", pattern.getFileName)
          result
        }).setRegistryName(ItemRhShield.getRegistryName.getResourcePath + pattern.getFileName))
        : _*
    )
  }

}
