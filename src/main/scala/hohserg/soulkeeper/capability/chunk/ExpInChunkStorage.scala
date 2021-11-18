package hohserg.soulkeeper.capability.chunk

import net.minecraft.nbt.{NBTBase, NBTTagCompound}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage

class ExpInChunkStorage extends IStorage[ExpInChunk] {
  override def writeNBT(capability: Capability[ExpInChunk], instance: ExpInChunk, side: EnumFacing): NBTBase = {
    val r = new NBTTagCompound
    r.setInteger("experience", instance.experience)
    r
  }

  override def readNBT(capability: Capability[ExpInChunk], instance: ExpInChunk, side: EnumFacing, nbt: NBTBase): Unit =
    instance.experience = nbt.asInstanceOf[NBTTagCompound].getInteger("experience")
}
