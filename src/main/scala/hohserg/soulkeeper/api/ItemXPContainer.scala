package hohserg.soulkeeper.api

import net.minecraft.item.{Item, ItemStack}

trait ItemXPContainer {
  self: Item =>

  def getXpCapacity(stack: ItemStack): Int

  def getXp(stack: ItemStack): Int =
    stack.getOrCreateSubCompound("xp_container").getInteger("xp")

  def setXp(stack: ItemStack, amount: Int): Unit =
    stack.getOrCreateSubCompound("xp_container").setInteger("xp", Math.max(0, Math.min(amount, getXpCapacity(stack))))

}
