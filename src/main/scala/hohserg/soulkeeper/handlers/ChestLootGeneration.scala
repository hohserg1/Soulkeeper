package hohserg.soulkeeper.handlers

import java.util.Random

import hohserg.soulkeeper.Configuration.{rhinestoneDustChestLootRarity, rhinestoneToolsChestLootRarity}
import hohserg.soulkeeper.Main
import hohserg.soulkeeper.capability.Capabilities.prevLootTable
import hohserg.soulkeeper.items.ItemRhinestoneDust
import net.minecraft.init.Items
import net.minecraft.inventory.{ContainerChest, InventoryLargeChest}
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.{TileEntity, TileEntityChest, TileEntityLockableLoot}
import net.minecraft.world.DimensionType
import net.minecraftforge.event.entity.player.{PlayerContainerEvent, PlayerInteractEvent}
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import Registration._
import hohserg.soulkeeper.capability.tile.PrevLootTable
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.AttachCapabilitiesEvent

@EventBusSubscriber(modid = Main.modid)
object ChestLootGeneration {

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

}
