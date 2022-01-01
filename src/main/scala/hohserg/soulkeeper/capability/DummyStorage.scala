package hohserg.soulkeeper.capability

import net.minecraft.nbt.NBTBase
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage

class DummyStorage[A] extends IStorage[A] {
  override def writeNBT(capability: Capability[A], instance: A, side: EnumFacing): NBTBase = ???

  override def readNBT(capability: Capability[A], instance: A, side: EnumFacing, nbt: NBTBase): Unit = ???
}
