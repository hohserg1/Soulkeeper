package hohserg.soulkeeper.capability.player

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.capability.Capabilities._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.common.capabilities.{Capability, ICapabilitySerializable}

class ExpInPlayerProvider(player: EntityPlayer) extends ICapabilitySerializable[NBTTagCompound] {
  val instance = new ExpInPlayer()
  instance.syncWithPlayer(player)

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean =
    capability == expInPlayer && facing == EnumFacing.UP

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T =
    expInPlayer.cast(if (hasCapability(capability, facing)) instance else null)

  override def serializeNBT(): NBTTagCompound =
    expInPlayer.writeNBT(instance, EnumFacing.UP).asInstanceOf[NBTTagCompound]

  override def deserializeNBT(nbt: NBTTagCompound): Unit =
    expInPlayer.readNBT(instance, EnumFacing.UP, nbt)
}

object ExpInPlayerProvider {
  val name = new ResourceLocation(Main.modid, "previous_xp")

  def getCapability(player: EntityPlayer): ExpInPlayer = player.getCapability(expInPlayer, EnumFacing.UP)
}
