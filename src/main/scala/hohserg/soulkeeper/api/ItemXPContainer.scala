package hohserg.soulkeeper.api

import hohserg.soulkeeper.items.tools.RhTool
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World

trait ItemXPContainer {
  self: Item =>

  def getXpCapacity(stack: ItemStack): Int

  def getXp(stack: ItemStack): Int =
    stack.getOrCreateSubCompound("xp_container").getInteger("xp")

  def setXp(stack: ItemStack, amount: Int): Unit =
    stack.getOrCreateSubCompound("xp_container").setInteger("xp", Math.max(0, Math.min(amount, getXpCapacity(stack))))

  override def addInformation(stack: ItemStack, worldIn: World, tooltip: java.util.List[String], flagIn: ITooltipFlag): Unit = {
    tooltip.add(I18n.format("soulkeeper.xp") + " " + getXp(stack) + "/" + stack.getMaxDamage)
  }

}
