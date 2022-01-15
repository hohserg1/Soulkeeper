package hohserg.soulkeeper.enchantments

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.items.tools.RhTool
import net.minecraft.enchantment.Enchantment.Rarity
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper, EnumEnchantmentType}
import net.minecraft.init.Enchantments
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.{EventPriority, SubscribeEvent}

@EventBusSubscriber(modid = Main.modid)
object EnchantInspire extends Enchantment(Rarity.COMMON, EnumEnchantmentType.BREAKABLE, Array()) {
  @SubscribeEvent(priority = EventPriority.LOWEST)
  def onPickupXpOrb(event: PlayerPickupXpEvent): Unit = {
    val stack = EnchantmentHelper.getEnchantedItem(this, event.getEntityPlayer)
    stack.getItem match {
      case _: RhTool =>
        val capa = CapabilityXPContainer(stack)
        val freeSpace = capa.getXpCapacity - capa.getXp
        val inspiredXp = math.min(freeSpace, event.getOrb.xpValue)
        capa.setXp(capa.getXp + inspiredXp)

        val mendedXp =
          if (EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack) > 0 && stack.isItemDamaged) {
            val ratio: Float = stack.getItem.getXpRepairRatio(stack)
            val freeDurabilitySpace: Int = stack.getItemDamage
            val xpRequired: Int = math.ceil(freeDurabilitySpace / ratio).toInt
            val xp: Int = math.min(xpRequired, event.getOrb.xpValue)
            val repaired = math.min(freeDurabilitySpace, math.floor(xp * ratio).toInt)

            stack.setItemDamage(stack.getItemDamage - repaired)

            xp

          } else
            0

        val consumedXp = math.max(inspiredXp, mendedXp)
        event.getOrb.xpValue -= consumedXp
      case _ =>
    }

  }

}
