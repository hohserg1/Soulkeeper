package hohserg.soulkeeper.api

import hohserg.soulkeeper.api.crafting.InfuserRecipe
import net.minecraftforge.fml.common.registry.GameRegistry

object Registries {
  lazy val INFUSER_RECIPES = GameRegistry.findRegistry(classOf[InfuserRecipe])

}
