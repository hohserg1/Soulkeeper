package hohserg.soulkeeper.capability.player

import net.minecraft.entity.player.EntityPlayer

class ExpInPlayer {
  var experienceLevel: Int = 0
  var experienceTotal: Int = 0
  var experience: Float = 0

  def syncWithPlayer(player: EntityPlayer) = {
    experienceLevel = player.experienceLevel
    experienceTotal = player.experienceTotal
    experience = player.experience
  }

}
