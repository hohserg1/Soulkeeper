package hohserg.soulkeeper.network

import codechicken.lib.packet.ICustomPacketHandler.IServerPacketHandler
import codechicken.lib.packet.PacketCustom
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.INetHandlerPlayServer

class ServerPacketHandler extends IServerPacketHandler{
  override def handlePacket(packetCustom: PacketCustom, entityPlayerMP: EntityPlayerMP, iNetHandlerPlayServer: INetHandlerPlayServer): Unit = ???
}
