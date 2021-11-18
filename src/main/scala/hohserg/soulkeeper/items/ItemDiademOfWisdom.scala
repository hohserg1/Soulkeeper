package hohserg.soulkeeper.items

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.api.events.ChangePlayerXPEvent
import hohserg.soulkeeper.utils.XPUtils
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemArmor
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

@EventBusSubscriber(modid = Main.modid)
object ItemDiademOfWisdom extends ItemArmor(ItemArmor.ArmorMaterial.GOLD, -1, EntityEquipmentSlot.HEAD) {

  //@SubscribeEvent
  def onConsumeXPLevel(e: ChangePlayerXPEvent): Unit = {
    if (e.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem == this)
      if (e.prevLevel > e.currLevel && (e.currLevel != 0 || e.currTotal != 0) && e.prevPartialLevel == e.currPartialLevel) {
        val lvlRequired = e.prevLevel - e.currTotal
        val firstLevelsXPAmount = XPUtils.getTotalXPForLevel(lvlRequired)
        e.player.experienceTotal = e.prevTotal - firstLevelsXPAmount
        e.player.experienceLevel = XPUtils.getLevelForTotalXP(e.player.experienceTotal)
      }
  }

  /*
  @SubscribeEvent
  def testAnvilRecipe(e: AnvilUpdateEvent): Unit =
    if (e.getLeft.getItem == Items.ENDER_PEARL && e.getRight.getItem == Items.BLAZE_POWDER && e.getRight.getCount >= e.getLeft.getCount) {
      e.setOutput(new ItemStack(Items.ENDER_EYE, e.getLeft.getCount))
      e.setMaterialCost(e.getLeft.getCount)
      e.setCost(10 * e.getLeft.getCount)
    }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  def anvilUpdateHandler(e: AnvilUpdateEvent): Unit = {
    println(s"AnvilUpdateEvent", AnvilUtils.getActualAnvilRecipe(e))
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  def anvilCraftHandler(e: AnvilRepairEvent): Unit = {
    println(s"AnvilRepairEvent ${e.getItemInput} + ${e.getIngredientInput} = ${e.getItemResult} | breakChance=${e.getBreakChance}")
  }*/
}
