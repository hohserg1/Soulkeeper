package hohserg.soulkeeper.api.crafting

import hohserg.soulkeeper.api.Registries
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.registries.IForgeRegistryEntry

import scala.collection.JavaConverters._

sealed trait InfuserRecipe extends IForgeRegistryEntry.Impl[InfuserRecipe] {
  def input: Item
}

object InfuserRecipe {

  def findRecipe(input: ItemStack): Option[InfuserRecipe] =
    recipeMap.get(input.getItem)

  private lazy val recipeMap: Map[Item, InfuserRecipe] =
    Registries.INFUSER_RECIPES.getValuesCollection.asScala
      .map(r => r.input -> r)
      .toMap

}

case class DummyInfuserRecipe(input: Item, output: ItemStack, xp: Int) extends InfuserRecipe

case class StepInfuserRecipe(input: Item, stepXP: Int, stepAmount: Int) extends InfuserRecipe