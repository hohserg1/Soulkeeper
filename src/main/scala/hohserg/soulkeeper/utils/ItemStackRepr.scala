package hohserg.soulkeeper.utils

import net.minecraft.item.{Item, ItemStack}

case class ItemStackRepr(item: Item, damage: Int)

object ItemStackRepr {
  def fromStack(stack: ItemStack): ItemStackRepr =
    ItemStackRepr(stack.getItem, stack.getItemDamage)

}
