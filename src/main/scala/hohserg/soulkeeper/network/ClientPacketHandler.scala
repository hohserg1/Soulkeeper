package hohserg.soulkeeper.network

import codechicken.lib.packet.ICustomPacketHandler.IClientPacketHandler
import codechicken.lib.packet.PacketCustom
import hohserg.soulkeeper.entities.CustomEntityXPOrb
import hohserg.soulkeeper.network.PacketTypes._
import net.minecraft.client.Minecraft
import net.minecraft.network.play.INetHandlerPlayClient

class ClientPacketHandler extends IClientPacketHandler {
  override def handlePacket(packetCustom: PacketCustom, minecraft: Minecraft, iNetHandlerPlayClient: INetHandlerPlayClient): Unit =
    PacketTypes.fromInt(packetCustom.getType) match {
      case SyncXPOrb =>
        val entityId = packetCustom.readInt()
        val entity = minecraft.world.getEntityByID(entityId)
        entity match {
          case orb: CustomEntityXPOrb =>
            orb.setXpValue(packetCustom.readInt())
          case _ =>
        }
      case _ =>
    }
}
