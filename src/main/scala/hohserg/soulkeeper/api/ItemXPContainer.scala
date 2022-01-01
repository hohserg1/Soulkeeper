package hohserg.soulkeeper.api

import hohserg.soulkeeper.items.tools.RhTool
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

trait ItemXPContainer {
  self: Item =>

  def getXpCapacity(stack: ItemStack): Int

  def getXp(stack: ItemStack): Int =
    stack.getOrCreateSubCompound("xp_container").getInteger("xp")

  def setXp(stack: ItemStack, amount: Int): Unit =
    stack.getOrCreateSubCompound("xp_container").setInteger("xp", Math.max(0, Math.min(amount, getXpCapacity(stack))))

}
