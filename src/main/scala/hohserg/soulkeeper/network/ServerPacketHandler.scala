package hohserg.soulkeeper.network

import codechicken.lib.packet.ICustomPacketHandler.IServerPacketHandler
import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.blocks.BlockRhOrb
import hohserg.soulkeeper.network.PacketTypes.ChangeRhOrbStep
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.INetHandlerPlayServer

class ServerPacketHandler extends IServerPacketHandler {
  override def handlePacket(packetCustom: PacketCustom, entityPlayerMP: EntityPlayerMP, iNetHandlerPlayServer: INetHandlerPlayServer): Unit = {
    PacketTypes.fromInt(packetCustom.getType) match {
      case ChangeRhOrbStep =>
        BlockRhOrb.onChangeStep(entityPlayerMP, packetCustom.readInt())

      case _ =>
    }
  }
}
