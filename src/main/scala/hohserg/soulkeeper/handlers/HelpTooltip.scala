package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.items.HasHelp
import net.minecraft.client.resources.I18n
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.init.Items
import net.minecraft.item.{ItemBlock, ItemEnchantedBook, ItemStack}
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

import scala.collection.JavaConverters._
import scala.collection.mutable

@EventBusSubscriber(modid = Main.modid)
object HelpTooltip {

  @SubscribeEvent
  def onTooltip(event: ItemTooltipEvent): Unit = {
    implicit val tooltip: mutable.Buffer[String] = event.getToolTip.asScala
    implicit val stack: ItemStack = event.getItemStack
    stack.getItem match {
      case item: HasHelp =>
        if (isShiftPressed)
          tooltip.insert(1, I18n.format("soulkeeper.help." + item.getRegistryName.getResourcePath))
        else
          tooltip.insert(1, I18n.format("soulkeeper.help.press_shift"))

      case itemblock: ItemBlock if itemblock.getBlock.isInstanceOf[HasHelp] =>
        if (isShiftPressed)
          tooltip.insert(1, I18n.format("soulkeeper.help." + itemblock.getRegistryName.getResourcePath))
        else
          tooltip.insert(1, I18n.format("soulkeeper.help.press_shift"))

      case Items.ENCHANTED_BOOK =>
        val enchantsTag = ItemEnchantedBook.getEnchantments(stack)
        val enchants =
          (0 until enchantsTag.tagCount())
            .map(enchantsTag.getCompoundTagAt)
            .map(i => Enchantment.getEnchantmentByID(i.getShort("id").toInt) -> i.getShort("lvl").toInt)
              .toMap
        addEnchantsHelp(enchants)

      case _ =>
    }

    if (stack.isItemEnchanted || stack.getItem != Items.ENCHANTED_BOOK)
      addEnchantsHelp(EnchantmentHelper.getEnchantments(stack).asScala.toMap.mapValues(_.toInt))
  }

  private def isShiftPressed: Boolean =
    Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)


  def addEnchantmentHelp(enchants: Map[Enchantment, Int], enchant: Enchantment)(implicit tooltip: mutable.Buffer[String]): Unit = {
    if (enchants.contains(enchant)) {
      val pos = tooltip.indexOf(enchant.getTranslatedName(enchants(enchant)))
      if (pos != -1)
        tooltip.insert(pos + 1, " - " + I18n.format("soulkeeper.help." + enchant.getRegistryName.getResourcePath))
    }
  }

  def addEnchantsHelp(enchants: Map[Enchantment, Int])(implicit stack: ItemStack, tooltip: mutable.Buffer[String]): Unit = {
    if (isShiftPressed)
      Registration.enchantments.foreach(addEnchantmentHelp(enchants, _))
    else if (Registration.enchantments.intersect(enchants.keySet).nonEmpty)
      tooltip.insert(1, I18n.format("soulkeeper.help.press_shift"))
  }

}
