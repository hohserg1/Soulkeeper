package hohserg.soulkeeper.items

import hohserg.soulkeeper.enchantments.{EnchantInspire, EnchantShiny, EnchantXPLeak}
import hohserg.soulkeeper.handlers.Registration
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.{EnchantmentData, EnchantmentHelper}
import net.minecraft.item.{Item, ItemEnchantedBook, ItemStack}
import net.minecraft.util.NonNullList

object ItemRhinestoneDust extends Item{
  override def getSubItems(tab: CreativeTabs, items: NonNullList[ItemStack]): Unit = {
    super.getSubItems(tab, items)
    if(tab==Registration.tab){
      items.add(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(EnchantShiny,1)))
      items.add(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(EnchantInspire,1)))
      items.add(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(EnchantXPLeak,3)))
    }
  }
}