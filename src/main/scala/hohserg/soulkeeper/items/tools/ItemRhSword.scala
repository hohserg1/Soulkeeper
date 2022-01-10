package hohserg.soulkeeper.items.tools

import net.minecraft.item.ItemSword

object ItemRhSword extends ItemSword(rhinestone) with RhTool {
  override def dustAmount: Int = 2
}
