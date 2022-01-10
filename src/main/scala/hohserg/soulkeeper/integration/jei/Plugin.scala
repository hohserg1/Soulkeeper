package hohserg.soulkeeper.integration.jei

import hohserg.soulkeeper.api.Registries
import hohserg.soulkeeper.api.crafting.InfuserRecipe
import hohserg.soulkeeper.blocks.BlockInfuser
import mezz.jei.api.recipe.{IRecipeCategoryRegistration, IRecipeWrapper, IRecipeWrapperFactory}
import mezz.jei.api.{IModPlugin, IModRegistry, JEIPlugin}
import net.minecraft.item.ItemStack

@JEIPlugin
class Plugin extends IModPlugin {
  override def registerCategories(registry: IRecipeCategoryRegistration): Unit = {
    registry.addRecipeCategories(new InfusionCategory(registry.getJeiHelpers.getGuiHelper))
  }

  override def register(registry: IModRegistry): Unit = {
    registry.addRecipes(Registries.INFUSER_RECIPES.getValuesCollection, InfusionCategory.id)
    registry.handleRecipes(classOf[InfuserRecipe], new IRecipeWrapperFactory[InfuserRecipe] {
      override def getRecipeWrapper(recipe: InfuserRecipe): IRecipeWrapper = new InfuserRecipeWrapper(recipe)
    }, InfusionCategory.id)
    registry.addRecipeCatalyst(new ItemStack(BlockInfuser), InfusionCategory.id)
  }

}
