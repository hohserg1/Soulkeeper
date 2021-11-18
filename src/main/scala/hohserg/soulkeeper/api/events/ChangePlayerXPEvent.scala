package hohserg.soulkeeper.api.events

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.Event

case class ChangePlayerXPEvent(
                                player: EntityPlayer,
                                prevLevel: Int, prevTotal: Int, prevPartialLevel: Float,
                                currLevel: Int, currTotal: Int, currPartialLevel: Float
                              ) extends Event
