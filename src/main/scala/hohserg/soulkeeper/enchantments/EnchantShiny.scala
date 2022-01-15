package hohserg.soulkeeper.enchantments

import net.minecraft.enchantment.Enchantment.Rarity
import net.minecraft.enchantment.{Enchantment, EnumEnchantmentType}
import net.minecraft.item.ItemStack

object EnchantShiny extends Enchantment(Rarity.VERY_RARE, EnumEnchantmentType.ALL, Array()) {

  override def getMinEnchantability(enchantmentLevel: Int): Int = 1

  def getShinyColor(stack: ItemStack): Int = Option(stack.getSubCompound("shiny")).filter(_.hasKey("color")).map(_.getInteger("color")).getOrElse(-8372020)

  def setShinyColor(stack: ItemStack, color: Int): Unit = stack.getOrCreateSubCompound("shiny").setInteger("color", color)
}
