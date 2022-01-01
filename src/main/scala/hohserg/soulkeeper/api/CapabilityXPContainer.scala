package hohserg.soulkeeper.api

import hohserg.soulkeeper.api.Capabilities._
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}

abstract class CapabilityXPContainer extends ICapabilityProvider {

  def getXpCapacity: Int

  def getXp: Int

  def setXp(amount: Int): Unit

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == CAPABILITY_XP_CONTAINER

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = CAPABILITY_XP_CONTAINER.cast(if (hasCapability(capability, facing)) this else null)
}

object CapabilityXPContainer {

  def apply(stack: ItemStack): CapabilityXPContainer =
    stack.getCapability(CAPABILITY_XP_CONTAINER, null)

  class Impl(private val stack: ItemStack) extends CapabilityXPContainer {
    private val item = stack.getItem.asInstanceOf[ItemXPContainer]

    override def getXpCapacity: Int = item.getXpCapacity(stack)

    override def getXp: Int = item.getXp(stack)

    override def setXp(amount: Int): Unit = item.setXp(stack, amount)

  }

}
