package hohserg.soulkeeper.utils

import scala.collection.mutable

object XPUtils {
  val xpLevelLimit = 21863

  val cache = new mutable.ArraySeq[Int](xpLevelLimit);
  {
    var current = 0
    for {
      i <- 0 until xpLevelLimit
    } {
      cache(i) = current
      current += getTotalXPDiffWithNextLevelForLevel(i)
    }
  }

  def getTotalXPForLevel(level: Int, partialLevel: Float = 0): Int = {
    (if (level <= 0)
      0
    else if (level >= cache.size)
      Int.MaxValue
    else
      cache(level)
      ) + partialLevel * getTotalXPDiffWithNextLevelForLevel(level)
  }.toInt

  def getTotalXPDiffWithNextLevelForLevel(level: Int): Int =
    if (level >= 30)
      112 + (level - 30) * 9
    else if (level >= 15)
      37 + (level - 15) * 5
    else
      7 + level * 2

  def getLevelForTotalXP(amount: Int): Int = {
    var targetXp = amount
    var level = 0
    while (true) {
      val xpToNextLevel = getTotalXPDiffWithNextLevelForLevel(level)
      if (targetXp < xpToNextLevel)
        return level
      level += 1
      targetXp -= xpToNextLevel
    }
    0
  }

}
