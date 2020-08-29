package hohserg.soulkeeper.capability

import hohserg.soulkeeper.Main
import hohserg.soulkeeper.capability.Capabilities.expInChunk
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.{Capability, ICapabilitySerializable}

class ExpInChunkProvider extends ICapabilitySerializable[NBTTagCompound] {

  val instance = new ExpInChunk

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == expInChunk && facing == EnumFacing.UP

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = expInChunk.cast(if (capability == expInChunk && facing == EnumFacing.UP) instance else null)

  override def serializeNBT(): NBTTagCompound =
    expInChunk.writeNBT(instance, EnumFacing.UP).asInstanceOf[NBTTagCompound]


  override def deserializeNBT(nbt: NBTTagCompound): Unit =
    expInChunk.readNBT(instance, EnumFacing.UP, nbt)
}

object ExpInChunkProvider {
  val name = new ResourceLocation(Main.modid, classOf[ExpInChunk].getSimpleName.toLowerCase)

  def getCapability(world: World, pos: BlockPos): ExpInChunk = world.getChunkFromBlockCoords(pos).getCapability(expInChunk, EnumFacing.UP)
}
