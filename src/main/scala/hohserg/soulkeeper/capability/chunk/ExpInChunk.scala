package hohserg.soulkeeper.capability.chunk

class ExpInChunk {

  var experience: Int = 0

  def consumeXP(amount: Int): Boolean = {
    if (experience >= amount) {
      experience -= amount
      true
    } else
      false
  }

}
