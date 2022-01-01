package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.{Main, XPUtils}
import hohserg.soulkeeper.items.tools.RhTool
import hohserg.soulkeeper.utils.AnvilUtils
import net.minecraft.advancements.{CriteriaTriggers, ICriterionTrigger, PlayerAdvancements}
import net.minecraft.advancements.critereon.{EnchantedItemTrigger, ItemPredicate, MinMaxBounds}
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.AnvilUpdateEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent

@EventBusSubscriber(modid = Main.modid)
object ToolInfusing {

  def onEnchanted(player: EntityPlayer, stack: ItemStack, level: Int): Unit = {
    val currXP = XPUtils.getPlayerXP(player)
    val prevXP = XPUtils.getExperienceForLevelAndBar(player.experienceLevel + level, player.experience)
    val enchantCost = prevXP - currXP
    stack.getItem match {
      case tool: RhTool => tool.setXp(stack, tool.getXp(stack) + enchantCost)
      case _ =>
    }
  }

  @SubscribeEvent
  def addEnchantHandler(event: PlayerLoggedInEvent): Unit = {
    event.player match {
      case player: EntityPlayerMP =>

        val uuid = player.getUniqueID
        val playerList = player.getServer.getPlayerList

        CriteriaTriggers.ENCHANTED_ITEM.addListener(
          player.getAdvancements,
          new ICriterionTrigger.Listener[EnchantedItemTrigger.Instance](
            new EnchantedItemTrigger.Instance(ItemPredicate.ANY, new MinMaxBounds(null, null)) {
              override def test(stack: ItemStack, levelsIn: Int): Boolean = {

                onEnchanted(playerList.getPlayerByUUID(uuid), stack, levelsIn)

                super.test(stack, levelsIn)
              }
            },
            player.getServerWorld.getAdvancementManager.getAdvancement(new ResourceLocation("minecraft", "story/enchant_item")),
            "enchantment_handler"
          ) {
            override def grantCriterion(playerAdvancementsIn: PlayerAdvancements): Unit = ()
          }
        )
      case _ =>
    }
  }

  @SubscribeEvent
  def onEnchantItemOnAnvil(event: AnvilUpdateEvent): Unit =
    event.getLeft.getItem match {
      case tool: RhTool =>
        AnvilUtils.getActualAnvilRecipe(event).foreach { recipe =>
          val result = recipe.result.copy()

          tool.setXp(result, tool.getXp(result) + XPUtils.getExperienceForLevel(recipe.xpCost))
          if (result.getEnchantmentTagList.equals(event.getLeft.getEnchantmentTagList))
            result.setRepairCost(event.getLeft.getRepairCost)

          event.setOutput(result)
          event.setCost(recipe.xpCost)
          event.setMaterialCost(recipe.right.getCount)
        }
      case _ =>
    }

}
