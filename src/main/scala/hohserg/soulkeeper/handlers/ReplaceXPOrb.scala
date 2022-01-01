package hohserg.soulkeeper.handlers

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import net.minecraft.entity.item.EntityXPOrb
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = Main.modid)
object ReplaceXPOrb {

  ///summon minecraft:xp_orb ~ ~-10 ~ {Value:7}
  ///summon minecraft:xp_orb ~-3 ~+3 ~ {Value:100}
  @SubscribeEvent
  def replaceXPOrbByCustom(event: EntityJoinWorldEvent): Unit = {
    val world = event.getWorld
    event.getEntity match {
      case _: CustomEntityXPOrb =>
      case orb: EntityXPOrb if !orb.isInstanceOf[CustomEntityXPOrb] =>
        event.setCanceled(true)
        if (!event.getWorld.isRemote)
          event.getWorld.spawnEntity(new CustomEntityXPOrb(orb))
      case _ =>
    }
  }

}
