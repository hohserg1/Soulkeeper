package hohserg.soulkeeper.enchantments

import net.minecraft.client.renderer.entity.layers.LayerArmorBase
import net.minecraft.enchantment.Enchantment.Rarity
import net.minecraft.enchantment.{Enchantment, EnumEnchantmentType}

object EnchantShining extends Enchantment(Rarity.VERY_RARE, EnumEnchantmentType.ALL, Array()) {

  override def getMinEnchantability(enchantmentLevel: Int): Int = 1

}
