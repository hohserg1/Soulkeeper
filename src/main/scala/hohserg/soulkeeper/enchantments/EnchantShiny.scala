package hohserg.soulkeeper.enchantments

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.utils.color.RGBA
import net.minecraft.enchantment.Enchantment.Rarity
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper, EnumEnchantmentType}
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.world.World
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.oredict.DyeUtils
import net.minecraftforge.registries.IForgeRegistryEntry

@EventBusSubscriber(modid = Main.modid)
object EnchantShiny extends Enchantment(Rarity.VERY_RARE, EnumEnchantmentType.ALL, Array()) {

  override def getMinEnchantability(enchantmentLevel: Int): Int = 1

  def getShinyColor(stack: ItemStack): Int = {
    val tag = stack.getSubCompound("shiny")
    if (tag != null && tag.hasKey("color"))
      tag.getInteger("color")
    else
      -8372020
  }

  def setShinyColor(stack: ItemStack, color: Int): Unit = stack.getOrCreateSubCompound("shiny").setInteger("color", color)

  def hasColorTag(stack: ItemStack): Boolean =
    stack.hasTagCompound &&
      stack.getTagCompound.hasKey("shiny", 10) &&
      stack.getSubCompound("shiny").hasKey("color", 99)

  @SubscribeEvent
  def registerColoringRecipe(event: RegistryEvent.Register[IRecipe]): Unit = {
    event.getRegistry.register(new IForgeRegistryEntry.Impl[IRecipe] with IRecipe {

      sealed trait IngredientType

      case object ShinyItem extends IngredientType

      case object Dye extends IngredientType

      case object Other extends IngredientType

      override def matches(inv: InventoryCrafting, worldIn: World): Boolean = {
        val ingredients = (0 until inv.getSizeInventory) map inv.getStackInSlot filter (!_.isEmpty) groupBy { stack =>
          if (DyeUtils.isDye(stack))
            Dye
          else if (EnchantmentHelper.getEnchantmentLevel(EnchantShiny, stack) > 0)
            ShinyItem
          else
            Other
        }

        !ingredients.contains(Other) && ingredients.contains(ShinyItem) && ingredients(ShinyItem).size == 1 && ingredients.contains(Dye)
      }

      override def getCraftingResult(inv: InventoryCrafting): ItemStack = {
        val ingredients = (0 until inv.getSizeInventory) map inv.getStackInSlot filter (!_.isEmpty) groupBy { stack =>
          if (DyeUtils.isDye(stack))
            Dye
          else
            ShinyItem
        }
        val result = ingredients(ShinyItem).head.copy()

        val colorsToMix: Seq[RGBA] =
          (if (hasColorTag(result)) {
            Seq(RGBA.fromARGB(getShinyColor(result)))
          } else
            Seq()) ++ ingredients(Dye).map(DyeUtils.colorFromStack(_).get().getColorValue).map(RGBA.fromRGB)

        val sum = colorsToMix.foldLeft((0f, 0f, 0f)) { case ((sr, sg, sb), c) => (sr + c.getRF, sg + c.getGF, sb + c.getBF) }

        val resultColor = new RGBA(sum._1 / colorsToMix.size, sum._2 / colorsToMix.size, sum._3 / colorsToMix.size)
          .toHSBA.setB(0.8f).setS(0.7f).toRGBA.argb()

        println(resultColor.toHexString)

        setShinyColor(result, resultColor)
        result
      }

      override def canFit(width: Int, height: Int): Boolean = width * height >= 2

      override def getRecipeOutput: ItemStack = ItemStack.EMPTY
    }.setRegistryName("shiny_coloring"))
  }
}
