package hohserg.soulkeeper.api.events

import com.google.common.annotations.Beta
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.Event

@Beta
case class ChangePlayerXPEvent(
                                player: EntityPlayer,
                                prevLevel: Int, prevTotal: Int, prevPartialLevel: Float,
                                currLevel: Int, currTotal: Int, currPartialLevel: Float
                              ) extends Event
