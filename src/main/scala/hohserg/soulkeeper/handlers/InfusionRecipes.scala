package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.api.crafting.{DummyInfuserRecipe, InfuserRecipe, StepInfuserRecipe}
import hohserg.soulkeeper.blocks.{BlockDarkRhinestonePowder, BlockRhOrb, BlockRhPowderOrb}
import hohserg.soulkeeper.items.ItemRhShield
import hohserg.soulkeeper.items.bottle.{ItemDustBottle, ItemEmptyBottle, ItemFilledBottle}
import hohserg.soulkeeper.items.tools._
import hohserg.soulkeeper.{Configuration, Main}
import net.minecraft.block.Block
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.RegistryBuilder

import scala.language.implicitConversions

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
      StepInfuserRecipe(item, 1, item.getMaxDamage).setRegistryName(item.getRegistryName)

    event.getRegistry.register(toolInfusion(ItemRhAxe))
    event.getRegistry.register(toolInfusion(ItemRhPickaxe))
    event.getRegistry.register(toolInfusion(ItemRhShovel))
    event.getRegistry.register(toolInfusion(ItemRhSword))
    event.getRegistry.register(toolInfusion(ItemRhShield))
    event.getRegistry.register(StepInfuserRecipe(BlockDarkRhinestonePowder, 10, 15).setRegistryName("powder_infusion"))
    event.getRegistry.register(DummyInfuserRecipe(ItemDustBottle, new ItemStack(ItemEmptyBottle), 1).setRegistryName("bottle_infusion_1"))
    event.getRegistry.register(DummyInfuserRecipe(BlockRhPowderOrb, new ItemStack(BlockRhOrb), 50).setRegistryName("bottle_infusion_2"))
    event.getRegistry.register(DummyInfuserRecipe(ItemEmptyBottle, new ItemStack(ItemFilledBottle), Configuration.rhinestoneBottleCapacity).setRegistryName("bottle_filling"))
  }

  private implicit def blockToItem(block:Block):Item = Item.getItemFromBlock(block)

}
