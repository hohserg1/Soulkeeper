package hohserg.soulkeeper.items.tools

import net.minecraft.item.{ItemPickaxe, ItemStack}

object ItemRhPickaxe extends ItemPickaxe(rhinestone) with RhTool {
  override def dustAmount: Int = 3
}
