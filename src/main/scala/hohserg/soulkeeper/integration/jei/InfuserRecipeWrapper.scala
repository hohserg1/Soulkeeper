package hohserg.soulkeeper.integration.jei

import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.api.crafting.{DummyInfuserRecipe, InfuserRecipe, StepInfuserRecipe}
import mezz.jei.api.gui.IGuiItemStackGroup
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack

import scala.collection.JavaConverters._

class InfuserRecipeWrapper(recipe: InfuserRecipe) extends IRecipeWrapper {
  var isg: IGuiItemStackGroup = _

  println("InfuserRecipeWrapper " + recipe.getRegistryName)

  val in = new ItemStack(recipe.input)

  val out: List[ItemStack] = recipe match {
    case DummyInfuserRecipe(input, output, xp) =>
      List(output)
    case StepInfuserRecipe(input, stepXP, stepAmount) =>
      (1 to stepAmount)
        .map(_ * stepXP)
        .map { xp =>
          val r = in.copy()
          CapabilityXPContainer(r).setXp(xp)
          r
        }
        .toList
  }

  val XP: () => Int = recipe match {
    case DummyInfuserRecipe(input, output, xp) =>
      () => xp
    case StepInfuserRecipe(input, stepXP, stepAmount) =>
      () => {
        stepXP
      }
  }

  override def getIngredients(ingredients: IIngredients): Unit = {
    ingredients.setInput(classOf[ItemStack], in)
    ingredients.setOutputs(classOf[ItemStack], out.asJava)
  }

  override def drawInfo(minecraft: Minecraft, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int): Unit = {
    recipe match {
      case DummyInfuserRecipe(input, output, xp) =>
        minecraft.fontRenderer.drawString("+" + xp + " XP", 0, 0, 0x00ffff)
      case StepInfuserRecipe(input, stepXP, stepAmount) =>
        val allStagesLookTime = 2000L
        val lastStageRepeats = 2 * out.size / 3
        val currentIndex = math.min(out.size - 1, (System.currentTimeMillis / math.max(1, (allStagesLookTime + lastStageRepeats) / (out.size + lastStageRepeats)) % (out.size + lastStageRepeats)).toInt)
        val currentOut = out(currentIndex)
        minecraft.fontRenderer.drawString("+" + CapabilityXPContainer(currentOut).getXp + " XP", 0, 0, 0x00ffff)
        isg.set(1, currentOut)
    }
  }
}
