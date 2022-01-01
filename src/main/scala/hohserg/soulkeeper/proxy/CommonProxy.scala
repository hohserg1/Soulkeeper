package hohserg.soulkeeper.proxy

import java.util.Random

import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.Configuration._
import hohserg.soulkeeper.api.crafting.{DummyInfuserRecipe, InfuserRecipe, StepInfuserRecipe}
import hohserg.soulkeeper.api.{CapabilityXPContainer, ItemXPContainer}
import hohserg.soulkeeper.blocks.BlockInfuser.TileInfuser
import hohserg.soulkeeper.blocks._
import hohserg.soulkeeper.capability.Capabilities._
import hohserg.soulkeeper.capability.chunk.{ExpInChunk, ExpInChunkProvider, ExpInChunkStorage}
import hohserg.soulkeeper.capability.player.{ExpInPlayer, ExpInPlayerStorage}
import hohserg.soulkeeper.capability.tile.PrevLootTable
import hohserg.soulkeeper.capability.{DummyFactory, DummyStorage}
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.items.bottle.{ItemDustBottle, ItemEmptyBottle, ItemFilledBottle}
import hohserg.soulkeeper.items.fake.ItemEmptyBottleCork
import hohserg.soulkeeper.items.tools._
import hohserg.soulkeeper.items.{ItemDebugXPMeter, ItemRhinestoneDust, ItemTinyRhinestoneDust}
import hohserg.soulkeeper.network.ServerPacketHandler
import hohserg.soulkeeper.utils.AnvilUtils
import hohserg.soulkeeper.worldgen.{SoulkeeperPlantGenerator, StalactiteGenerator}
import hohserg.soulkeeper.{Configuration, Main, XPUtils}
import net.minecraft.advancements._
import net.minecraft.advancements.critereon.{EnchantedItemTrigger, ItemPredicate, MinMaxBounds}
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.init.Items
import net.minecraft.inventory.{ContainerChest, InventoryLargeChest}
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.tileentity.{TileEntity, TileEntityChest, TileEntityLockableLoot}
import net.minecraft.util.ResourceLocation
import net.minecraft.world.DimensionType
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.event.entity.player.{PlayerContainerEvent, PlayerInteractEvent}
import net.minecraftforge.event.entity.{EntityEvent, EntityJoinWorldEvent}
import net.minecraftforge.event.{AnvilUpdateEvent, AttachCapabilitiesEvent, RegistryEvent}
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent
import net.minecraftforge.fml.common.registry.{EntityEntry, EntityEntryBuilder, GameRegistry}
import net.minecraftforge.registries.RegistryBuilder

class CommonProxy {

  def preInit(event: FMLPreInitializationEvent): Unit = {
    PacketCustom.assignHandler(Main.modid, new ServerPacketHandler)
    MinecraftForge.EVENT_BUS.register(this)
    FMLCommonHandler.instance().bus().register(this)
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

  lazy val tab = new CreativeTabs(Main.modid) {
    override def getTabIconItem: ItemStack = new ItemStack(BlockSoulkeeperPlant)
  }

  lazy val blocks = Seq(BlockDarkRhinestone, BlockSoulkeeperPlant, BlockDarkRhinestonePowder, BlockDarkRhinestoneStalactite, BlockInfuser)
  lazy val tools = Seq(ItemRhPickaxe, ItemRhAxe, ItemRhShovel, ItemRhSword)
  lazy val items = Seq(ItemTinyRhinestoneDust, ItemRhinestoneDust, ItemDebugXPMeter, ItemDustBottle, ItemEmptyBottle, ItemFilledBottle) ++ blocks.map(new ItemBlock(_)) ++ tools ++ Seq(ItemEmptyBottleCork)

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

  @SubscribeEvent
  def onAttachCapaToChunk(event: AttachCapabilitiesEvent[Chunk]): Unit =
    event.addCapability(ExpInChunkProvider.name, new ExpInChunkProvider)

  ///summon minecraft:xp_orb ~ ~-10 ~ {Value:7}
  ///summon minecraft:xp_orb ~-3 ~+3 ~ {Value:100}
  @SubscribeEvent
  def onExpOrbWorldEnter(event: EntityJoinWorldEvent): Unit = {
    val world = event.getWorld
    event.getEntity match {
      case _: CustomEntityXPOrb =>
      case orb: EntityXPOrb if !orb.isInstanceOf[CustomEntityXPOrb] =>
        event.setCanceled(true)
        if (!event.getWorld.isRemote)
          event.getWorld.spawnEntity(new CustomEntityXPOrb(orb))
      case _ =>
    }
  }

  @SubscribeEvent
  def onPlayerDropExp(event: LivingExperienceDropEvent): Unit =
    event.getEntity match {
      case player: EntityPlayer =>
        ExpInChunkProvider.getCapability(player.world, player.getPosition)
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

  def onEnchanted(player: EntityPlayer, stack: ItemStack, level: Int): Unit = {
    val currXP = XPUtils.getPlayerXP(player)
    val prevXP = XPUtils.getExperienceForLevelAndBar(player.experienceLevel + level, player.experience)
    val enchantCost = prevXP - currXP
    stack.getItem match {
      case tool: RhTool => tool.setXp(stack, tool.getXp(stack) + enchantCost)
      case _ =>
    }
  }

  @SubscribeEvent
  def addEnchantHandler(event: PlayerLoggedInEvent): Unit = {
    event.player match {
      case player: EntityPlayerMP =>

        val uuid = player.getUniqueID
        val playerList = player.getServer.getPlayerList

        CriteriaTriggers.ENCHANTED_ITEM.addListener(
          player.getAdvancements,
          new ICriterionTrigger.Listener[EnchantedItemTrigger.Instance](
            new EnchantedItemTrigger.Instance(ItemPredicate.ANY, new MinMaxBounds(null, null)) {
              override def test(stack: ItemStack, levelsIn: Int): Boolean = {

                onEnchanted(playerList.getPlayerByUUID(uuid), stack, levelsIn)

                super.test(stack, levelsIn)
              }
            },
            player.getServerWorld.getAdvancementManager.getAdvancement(new ResourceLocation("minecraft", "story/enchant_item")),
            "enchantment_handler"
          ) {
            override def grantCriterion(playerAdvancementsIn: PlayerAdvancements): Unit = ()
          }
        )
      case _ =>
    }
  }

  @SubscribeEvent
  def onEnchantItemOnAnvil(event: AnvilUpdateEvent): Unit =
    event.getLeft.getItem match {
      case tool: RhTool =>
        AnvilUtils.getActualAnvilRecipe(event).foreach { recipe =>
          val result = recipe.result.copy()

          tool.setXp(result, tool.getXp(result) + XPUtils.getExperienceForLevel(recipe.xpCost))
          if (result.getEnchantmentTagList.equals(event.getLeft.getEnchantmentTagList))
            result.setRepairCost(event.getLeft.getRepairCost)

          event.setOutput(result)
          event.setCost(recipe.xpCost)
          event.setMaterialCost(recipe.right.getCount)
        }
      case _ =>
    }

  @SubscribeEvent
  def attachCapaToTile(event: AttachCapabilitiesEvent[TileEntity]): Unit =
    if (event.getObject.isInstanceOf[TileEntityLockableLoot])
      event.addCapability(new ResourceLocation(Main.modid, "prev_loot_table"), new PrevLootTable)

  @SubscribeEvent
  def rightClickChest(event: PlayerInteractEvent.RightClickBlock): Unit = {
    if (!event.getWorld.isRemote) {
      event.getWorld.getTileEntity(event.getPos) match {
        case tile: TileEntityLockableLoot =>
          Option(tile.getCapability(prevLootTable, null))
            .foreach(_.lootTable = tile.getLootTable)

        case _ =>
      }
    }
  }

  @SubscribeEvent
  def generateLootInChest(event: PlayerContainerEvent.Open): Unit = {

    def getRandomEmptySlotAround(slot: Int, tile: TileEntityChest): Option[Int] = {
      val x = slot % 9
      val y = slot / 9
      val possibleSlots = List(
        (x - 1, y + 1), (x, y + 1), (x + 1, y + 1)
      )
        .filter { case (xx, yy) => xx >= 0 && xx <= 8 && yy >= 0 && yy <= 2 }
        .map { case (xx, yy) => yy * 9 + xx }
        .filter(!tile.getStackInSlot(_).isEmpty)

      if (possibleSlots.isEmpty)
        None
      else
        Some(possibleSlots(tile.getWorld.rand.nextInt(possibleSlots.size)))
    }

    def randomTool(rand: Random) = {
      val item = tools(rand.nextInt(tools.size))
      val r = new ItemStack(item)
      item.setXp(r, rand.nextInt(10))
      r
    }

    def randomElement[A](l: Seq[A], rand: Random): Option[A] =
      if (l.nonEmpty)
        Some(l(rand.nextInt(l.size)))
      else
        None

    if (event.getEntityPlayer.world.provider.getDimensionType == DimensionType.OVERWORLD)
      event.getContainer match {
        case chest: ContainerChest =>
          chest.lowerChestInventory match {
            case tile: TileEntityChest =>
              Option(tile.getCapability(prevLootTable, null))
                .foreach { prev =>
                  if (prev.lootTable != null) {
                    prev.lootTable = null
                    val rand = tile.getWorld.rand
                    if (rand.nextInt(rhinestoneDustChestLootRarity) == 0) {
                      randomElement(
                        (0 until tile.getSizeInventory)
                          .filter(i => tile.getStackInSlot(i).isEmpty)
                          .flatMap(i => getRandomEmptySlotAround(i, tile).map(i -> _)),
                        rand
                      )
                        .foreach { case (dustSlot, stickSlot) =>
                          tile.setInventorySlotContents(dustSlot, new ItemStack(ItemRhinestoneDust, rand.nextInt(3) + 1))
                          tile.setInventorySlotContents(stickSlot, new ItemStack(Items.STICK))
                        }
                    }
                    if (rand.nextInt(rhinestoneToolsChestLootRarity) == 0) {
                      randomElement(
                        (0 until tile.getSizeInventory).filter(i => tile.getStackInSlot(i).isEmpty),
                        rand
                      ).foreach(tile.setInventorySlotContents(_, randomTool(rand)))
                    }
                  }
                }
            case large: InventoryLargeChest =>
            case _ =>
          }
        case _ =>
      }
  }

  @SubscribeEvent
  def registerInfuserRecipesRegistry(event: RegistryEvent.NewRegistry): Unit = {
    new RegistryBuilder()
      .setName(new ResourceLocation(Main.modid, "infuser_recipes"))
      .setType(classOf[InfuserRecipe])
      .create()
  }

  @SubscribeEvent
  def registerInfuserRecipes(event: RegistryEvent.Register[InfuserRecipe]): Unit = {
    def toolInfusion(item: Item with RhTool): StepInfuserRecipe =
      StepInfuserRecipe(Ingredient.fromItem(item), 1, item.getMaxDamage)

    event.getRegistry.register(toolInfusion(ItemRhAxe))
    event.getRegistry.register(toolInfusion(ItemRhPickaxe))
    event.getRegistry.register(toolInfusion(ItemRhShovel))
    event.getRegistry.register(toolInfusion(ItemRhSword))
    event.getRegistry.register(StepInfuserRecipe(Ingredient.fromItem(Item.getItemFromBlock(BlockDarkRhinestonePowder)), 10, 15))
    event.getRegistry.register(DummyInfuserRecipe(Ingredient.fromStacks(new ItemStack(BlockDarkRhinestonePowder)), new ItemStack(BlockDarkRhinestone), 10))
    event.getRegistry.register(DummyInfuserRecipe(Ingredient.fromItem(ItemDustBottle), new ItemStack(ItemEmptyBottle), 1))
    event.getRegistry.register(DummyInfuserRecipe(Ingredient.fromItem(ItemEmptyBottle), new ItemStack(ItemFilledBottle), Configuration.rhinestoneBottleCapacity))
  }

  @SubscribeEvent
  def attachCapaToStacks(event: AttachCapabilitiesEvent[ItemStack]): Unit = {
    if (event.getObject.getItem.isInstanceOf[ItemXPContainer])
      event.addCapability(new ResourceLocation(Main.modid, "capa_xp_container"), new CapabilityXPContainer(event.getObject))
  }

}
