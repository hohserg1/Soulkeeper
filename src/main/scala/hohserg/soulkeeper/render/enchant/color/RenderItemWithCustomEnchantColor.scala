package hohserg.soulkeeper.render.enchant.color

import hohserg.soulkeeper.enchantments.EnchantShiny
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.{RenderItem, RenderItemDelegate}
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemStack

class RenderItemWithCustomEnchantColor(base: RenderItem) extends RenderItemDelegate(base) {
  override def renderItem(stack: ItemStack, model: IBakedModel): Unit = {
    if (!stack.isEmpty && !model.isBuiltInRenderer && EnchantmentHelper.getEnchantmentLevel(EnchantShiny, stack) > 0) {
      CustomEnchantColorRenderFactory.instance.renderShinyItem(stack, model, EnchantShiny.getShinyColor(stack))
    } else
      super.renderItem(stack, model)
  }
}
