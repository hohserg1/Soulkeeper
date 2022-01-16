package hohserg.soulkeeper.items.bottle

import hohserg.soulkeeper.items.HasHelp
import hohserg.soulkeeper.{Configuration, Main, XPUtils}
import net.minecraft.item.Item
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = Main.modid)
object ItemEmptyBottle extends Item with HasHelp{
  setMaxStackSize(64)

  @SubscribeEvent
  def onRightClick(event: PlayerInteractEvent.RightClickItem): Unit = {
    if (!event.getWorld.isRemote) {
      val stack = event.getItemStack
      if (stack.getItem == this) {
        val player = event.getEntityPlayer
        val curXP = XPUtils.getPlayerXP(player)
        if (curXP >= Configuration.rhinestoneBottleCapacity) {
          stack.shrink(1)
          XPUtils.setPlayerXP(player, curXP - Configuration.rhinestoneBottleCapacity)

          val filled = ItemFilledBottle.stackWithAmount(Configuration.rhinestoneBottleCapacity)

          if (!player.inventory.addItemStackToInventory(filled))
            player.dropItem(filled, false)
        }
      }
    }
  }
}
