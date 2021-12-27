package hohserg.soulkeeper.items.bottle

import hohserg.soulkeeper.{Configuration, XPUtils}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand}
import net.minecraft.world.World

object ItemEmptyBottle extends Item {
  setMaxStackSize(64)


  override def onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult[ItemStack] = {
    println("onItemRightClick")
    val stack = playerIn.getHeldItem(handIn)
    val curXP = XPUtils.getPlayerXP(playerIn)
    if (curXP >= Configuration.rhinestoneBottleCapacity) {
      stack.shrink(1)
      XPUtils.setPlayerXP(playerIn, curXP - Configuration.rhinestoneBottleCapacity)

      val filled = ItemFilledBottle.stackWithAmount(Configuration.rhinestoneBottleCapacity)

      if (!playerIn.inventory.addItemStackToInventory(filled))
        playerIn.dropItem(filled, false)

    }
    new ActionResult[ItemStack](EnumActionResult.PASS, stack)
  }

}
