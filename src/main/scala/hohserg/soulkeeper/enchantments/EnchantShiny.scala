package hohserg.soulkeeper.enchantments

import net.minecraft.enchantment.Enchantment.Rarity
import net.minecraft.enchantment.{Enchantment, EnumEnchantmentType}
import net.minecraft.item.ItemStack

object EnchantShiny extends Enchantment(Rarity.VERY_RARE, EnumEnchantmentType.ALL, Array()) {

  override def getMinEnchantability(enchantmentLevel: Int): Int = 1

  def getShiningColor(stack: ItemStack): Int = 0xffff0000
}
