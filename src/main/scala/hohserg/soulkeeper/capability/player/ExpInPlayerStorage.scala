package hohserg.soulkeeper.capability.player

import net.minecraft.nbt.{NBTBase, NBTTagCompound}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage

class ExpInPlayerStorage extends IStorage[ExpInPlayer] {
  override def writeNBT(capability: Capability[ExpInPlayer], instance: ExpInPlayer, side: EnumFacing): NBTBase = {
    val r = new NBTTagCompound

    r.setInteger("experienceLevel", instance.experienceLevel)
    r.setInteger("experienceTotal", instance.experienceTotal)
    r.setFloat("experience", instance.experience)

    r
  }

  override def readNBT(capability: Capability[ExpInPlayer], instance: ExpInPlayer, side: EnumFacing, nbt: NBTBase): Unit = {
    val r = nbt.asInstanceOf[NBTTagCompound]

    instance.experienceLevel = r.getInteger("experienceLevel")
    instance.experienceTotal = r.getInteger("experienceTotal")
    instance.experience = r.getFloat("experience")
  }
}
