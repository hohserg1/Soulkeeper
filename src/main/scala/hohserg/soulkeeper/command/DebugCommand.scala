package hohserg.soulkeeper.command

import hohserg.soulkeeper.utils.XPUtils
import net.minecraft.command.{CommandBase, ICommandSender}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.server.MinecraftServer

class DebugCommand extends CommandBase {
  override def getName: String = "sk"

  override def getUsage(sender: ICommandSender): String = "/sk xp [set/add] <amount>"

  override def execute(server: MinecraftServer, sender: ICommandSender, args: Array[String]): Unit = {
    if (args.size >= 3) {
      sender match {
        case player: EntityPlayer =>
          args(0) match {
            case "xp" =>
              args(1) match {
                case "set" =>
                  player.experienceLevel = args(2).toInt
                  player.experienceTotal = XPUtils.getTotalXPForLevel(player.experienceLevel)
                  player.experience = 0
                case "add" =>
                case _ =>
              }
            case _ =>
          }
        case _ =>
      }
    }
  }
}
