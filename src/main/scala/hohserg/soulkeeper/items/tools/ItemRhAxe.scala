package hohserg.soulkeeper.items.tools

import net.minecraft.item.ItemAxe

object ItemRhAxe extends ItemAxe(rhinestone, 8, -3.1f) with RhTool  {
  override def dustAmount: Int = 3


}
