package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.api.{CapabilityXPContainer, ItemXPContainer}
import net.minecraft.client.resources.I18n
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = Main.modid)
object XPContainer {
  @SubscribeEvent
  def attachCapaToStacks(event: AttachCapabilitiesEvent[ItemStack]): Unit = {
    if (event.getObject.getItem.isInstanceOf[ItemXPContainer])
      event.addCapability(new ResourceLocation(Main.modid, "capa_xp_container"), new CapabilityXPContainer.Impl(event.getObject))
  }

  @SubscribeEvent
  def addTooltip(event: ItemTooltipEvent): Unit = {
    val capa = CapabilityXPContainer(event.getItemStack)
    if (capa != null)
      event.getToolTip.add(1, I18n.format("soulkeeper.xp") + " " + capa.getXp + "/" + capa.getXpCapacity)
  }

}
