package hohserg.soulkeeper.api.crafting

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraftforge.registries.IForgeRegistryEntry

trait InfuserRecipe extends IForgeRegistryEntry.Impl[InfuserRecipe]{
  def input: Ingredient
}

case class DummyInfuserRecipe(input: Ingredient, output: ItemStack, xp: Int) extends InfuserRecipe

case class StepInfuserRecipe(input: Ingredient, stepXP: Int, stepAmount: Int) extends InfuserRecipe