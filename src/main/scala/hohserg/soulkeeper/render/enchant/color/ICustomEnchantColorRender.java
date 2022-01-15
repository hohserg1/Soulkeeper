package hohserg.soulkeeper.render.enchant.color;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface ICustomEnchantColorRender {
    void renderShinyGlint(RenderLivingBase<?> render, EntityLivingBase entity, ModelBase model, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale,
                          float r, float g, float b);

    void renderShinyItem(ItemStack stack, IBakedModel model, int color);
}
