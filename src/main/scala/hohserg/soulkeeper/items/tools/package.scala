package hohserg.soulkeeper.items

import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.EnumHelper

package object tools {
  lazy val rhinestone = EnumHelper.addToolMaterial("rhinestone", 2, 1500, 8, 3, 25)
    .setRepairItem(new ItemStack(ItemTinyRhinestoneDust))
}
