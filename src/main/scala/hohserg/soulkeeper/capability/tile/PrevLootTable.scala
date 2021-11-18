package hohserg.soulkeeper.capability.tile

import hohserg.soulkeeper.capability.Capabilities._
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}

class PrevLootTable extends ICapabilityProvider {
  var lootTable: ResourceLocation = _

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean =
    capability == prevLootTable

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T =
    prevLootTable.cast(if (hasCapability(capability, facing)) this else null)
}
