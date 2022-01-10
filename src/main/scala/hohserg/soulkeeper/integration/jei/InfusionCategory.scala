package hohserg.soulkeeper.integration.jei

import hohserg.soulkeeper.Main
import mezz.jei.api.IGuiHelper
import mezz.jei.api.gui.{IDrawable, IGuiItemStackGroup, IRecipeLayout}
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.IRecipeCategory
import mezz.jei.gui.elements.DrawableBuilder
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

class InfusionCategory(getGuiHelper: IGuiHelper) extends IRecipeCategory[InfuserRecipeWrapper] {
  override def getUid: String = InfusionCategory.id

  override def getTitle: String = "Experience Infusing"

  override def getModName: String = "Soulkeeper"

  override def getBackground: IDrawable = new DrawableBuilder(new ResourceLocation(Main.modid, "textures/gui/jei_infusion.png"), 0, 0, 100, 40).setTextureSize(100, 40).build()

  override def setRecipe(layout: IRecipeLayout, recipe: InfuserRecipeWrapper, iIngredients: IIngredients): Unit = {
    val isg = layout.getItemStacks
    recipe.isg=isg

    isg.init(0, true, 8, 11)
    isg.set(0, recipe.in)

    isg.init(1, false, 74, 11)
    isg.set(1, recipe.out.head)

  }
}

object InfusionCategory {
  val id = Main.modid + ":infusion"

}
