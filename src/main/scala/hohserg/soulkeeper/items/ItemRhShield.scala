package hohserg.soulkeeper.items

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.items.tools.{RhTool, rhinestone}
import net.minecraft.advancements.critereon.{ItemDurabilityTrigger, ItemPredicate, MinMaxBounds}
import net.minecraft.advancements.{CriteriaTriggers, ICriterionTrigger, PlayerAdvancements}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.{ItemShield, ItemStack}
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.translation.I18n
import net.minecraft.world.World
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent

import java.util

@EventBusSubscriber(modid = Main.modid)
object ItemRhShield extends ItemShield with RhTool {

  override def getItemStackDisplayName(stack: ItemStack): String =
    I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name").trim

  override def getIsRepairable(toRepair: ItemStack, repair: ItemStack): Boolean =
    repair.getItem == ItemTinyRhinestoneDust || repair.getItem == ItemRhinestoneDust

  override def dustAmount: Int = 5

  override def isShield(stack: ItemStack, entity: EntityLivingBase): Boolean = true

  override def getItemEnchantability: Int = rhinestone.getEnchantability

  def onDamageItem(player: EntityPlayerMP, stack: ItemStack, currentDamage: Int, newDamage: Int): Unit = {
    if (stack.getItem == this) {
      val damage = newDamage - currentDamage
      onUseItem(stack, player, damage)
      stack.setItemDamage(stack.getItemDamage - damage)
    }
  }

  override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[String], flagIn: ITooltipFlag): Unit = {
    super.addInformation(stack, worldIn, tooltip, flagIn)
    val pattern = stack.getOrCreateSubCompound("shieldData").getString("pattern")
    tooltip.add("Pattern: " + (if (pattern == "") "soulkeeper" else pattern))
  }

  @SubscribeEvent
  def addItemDamageHandler(event: PlayerLoggedInEvent): Unit = {

    event.player match {
      case player: EntityPlayerMP =>

        val uuid = player.getUniqueID
        val playerList = player.getServer.getPlayerList

        val predicate = new ItemDurabilityTrigger.Instance(ItemPredicate.ANY, new MinMaxBounds(null, null), new MinMaxBounds(null, null)) {
          override def test(item: ItemStack, newDamage: Int): Boolean = {
            onDamageItem(playerList.getPlayerByUUID(uuid), item, item.getItemDamage, newDamage)

            super.test(item, newDamage)
          }
        }

        val vanillaAchivementId = new ResourceLocation("minecraft", "husbandry/break_diamond_hoe")

        val listenerName = "item_damage_handler"

        val hash = 31 * (31 * predicate.hashCode + vanillaAchivementId.hashCode) + listenerName.hashCode

        CriteriaTriggers.ITEM_DURABILITY_CHANGED.addListener(
          player.getAdvancements,
          new ICriterionTrigger.Listener[ItemDurabilityTrigger.Instance](predicate, player.getServerWorld.getAdvancementManager.getAdvancement(vanillaAchivementId), listenerName) {
            override def grantCriterion(playerAdvancementsIn: PlayerAdvancements): Unit = ()

            override def equals(other: Any): Boolean = other == this

            override def hashCode(): Int = hash
          }
        )

      case _ =>
    }
  }
}
