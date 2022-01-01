package hohserg.soulkeeper.items.fake

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.NonNullList

object ItemEmptyBottleCork extends Item {
  override def getSubItems(tab: CreativeTabs, items: NonNullList[ItemStack]): Unit = ()

}
