package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.{Configuration, Main}
import hohserg.soulkeeper.api.crafting.{DummyInfuserRecipe, InfuserRecipe, StepInfuserRecipe}
import hohserg.soulkeeper.blocks.{BlockDarkRhinestone, BlockDarkRhinestonePowder}
import hohserg.soulkeeper.items.bottle.{ItemDustBottle, ItemEmptyBottle, ItemFilledBottle}
import hohserg.soulkeeper.items.tools._
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.item.crafting.Ingredient
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.RegistryBuilder

@EventBusSubscriber(modid = Main.modid)
object InfusionRecipes {

  @SubscribeEvent
  def registerInfuserRecipesRegistry(event: RegistryEvent.NewRegistry): Unit = {
    new RegistryBuilder()
      .setName(new ResourceLocation(Main.modid, "infuser_recipes"))
      .setType(classOf[InfuserRecipe])
      .create()
  }

  @SubscribeEvent
  def registerInfuserRecipes(event: RegistryEvent.Register[InfuserRecipe]): Unit = {
    def toolInfusion(item: Item with RhTool): InfuserRecipe =
      StepInfuserRecipe(Ingredient.fromItem(item), 1, item.getMaxDamage).setRegistryName(item.getRegistryName)

    event.getRegistry.register(toolInfusion(ItemRhAxe))
    event.getRegistry.register(toolInfusion(ItemRhPickaxe))
    event.getRegistry.register(toolInfusion(ItemRhShovel))
    event.getRegistry.register(toolInfusion(ItemRhSword))
    event.getRegistry.register(StepInfuserRecipe(Ingredient.fromItem(Item.getItemFromBlock(BlockDarkRhinestonePowder)), 10, 15).setRegistryName("powder_infusion_1"))
    event.getRegistry.register(DummyInfuserRecipe(Ingredient.fromStacks(new ItemStack(BlockDarkRhinestonePowder, 1, 15)), new ItemStack(BlockDarkRhinestone), 10).setRegistryName("powder_infusion_2"))
    event.getRegistry.register(DummyInfuserRecipe(Ingredient.fromItem(ItemDustBottle), new ItemStack(ItemEmptyBottle), 1).setRegistryName("bottle_infusion"))
    event.getRegistry.register(DummyInfuserRecipe(Ingredient.fromItem(ItemEmptyBottle), new ItemStack(ItemFilledBottle), Configuration.rhinestoneBottleCapacity).setRegistryName("bottle_filling"))
  }

}
