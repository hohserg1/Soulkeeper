package hohserg.soulkeeper.items.tools

import net.minecraft.item.ItemSpade

object ItemRhShovel extends ItemSpade(rhinestone) with RhTool  {
  override def dustAmount: Int = 1
}
