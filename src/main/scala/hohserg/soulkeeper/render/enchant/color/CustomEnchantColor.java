package hohserg.soulkeeper.render.enchant.color;

import hohserg.soulkeeper.enchantments.EnchantShiny;
import hohserg.soulkeeper.enchantments.EnchantShiny$;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class CustomEnchantColor {

    public static void renderEnchGlint(
            RenderLivingBase render, EntityLivingBase entity, ModelBase model,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch, float scale,
            EntityEquipmentSlot slot
    ) {

        ItemStack stack = entity.getItemStackFromSlot(slot);
        if (EnchantmentHelper.getEnchantmentLevel(EnchantShiny$.MODULE$, stack) > 0) {
            int color = EnchantShiny.getShinyColor(stack);

            float r = (color >> 16 & 255) / 256f;
            float g = (color >> 8 & 255) / 256f;
            float b = (color & 255) / 256f;

            CustomEnchantColorRenderFactory.instance().renderShinyGlint(render, entity, model,
                    limbSwing, limbSwingAmount, partialTicks, ageInTicks,
                    netHeadYaw, headPitch, scale, r, g, b);
        } else
            LayerArmorBase.renderEnchantedGlint(render, entity, model, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
    }
}
