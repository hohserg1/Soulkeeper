package hohserg.soulkeeper.capability.tile

import com.enderio.core.common.util.FluidUtil
import crazypants.enderio.base.fluid.Fluids
import crazypants.enderio.base.xp.XpUtil
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.capability.tile.XpFluidCapa._
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandlerItem, IFluidTankProperties}
import net.minecraftforge.fluids.{FluidStack, FluidTankInfo, IFluidTank}

class XpFluidCapa(xpCapa: CapabilityXPContainer, val getContainer: ItemStack) extends IFluidTank with IFluidHandlerItem {
  override def getFluid: FluidStack =
    new FluidStack(Fluids.XP_JUICE.getFluid, getFluidAmount)

  override def getFluidAmount: Int =
    XpUtil.experienceToLiquid(xpCapa.getXp)

  override def getCapacity: Int =
    XpUtil.experienceToLiquid(xpCapa.getXpCapacity)

  override def getInfo: FluidTankInfo =
    new FluidTankInfo(new FluidStack(Fluids.XP_JUICE.getFluid, getFluidAmount), getCapacity)

  override lazy val getTankProperties: Array[IFluidTankProperties] = Array[IFluidTankProperties](new XpFluidTankProperties(this))

  override def fill(resource: FluidStack, doFill: Boolean): Int = {
    if (resource != null && resource.amount > 0 && isValidFluid(resource)) {
      val insertionAmountXP = XpUtil.liquidToExperience(resource.amount)
      val freeSpaceXP = xpCapa.getXpCapacity - xpCapa.getXp
      val canInsertXP = Math.min(freeSpaceXP, insertionAmountXP)
      if (canInsertXP > 0) {
        if (doFill)
          xpCapa.setXp(xpCapa.getXp + canInsertXP)
        XpUtil.experienceToLiquid(canInsertXP)
      } else
        0
    } else
      0
  }

  override def drain(resource: FluidStack, doDrain: Boolean): FluidStack =
    if (isValidFluid(resource))
      drain(resource.amount, doDrain)
    else
      null

  override def drain(maxDrainFluid: Int, doDrain: Boolean): FluidStack = {
    if (maxDrainFluid > 0) {
      val extractionAmountXP = XpUtil.liquidToExperience(maxDrainFluid)
      val canExtractXP = Math.min(xpCapa.getXp, extractionAmountXP)
      if (canExtractXP > 0) {
        if (doDrain)
          xpCapa.setXp(xpCapa.getXp - canExtractXP)
        new FluidStack(Fluids.XP_JUICE.getFluid, XpUtil.experienceToLiquid(canExtractXP))
      } else
        null
    } else
      null
  }
}

object XpFluidCapa {

  class Provider(xpCapa: CapabilityXPContainer, stack: ItemStack) extends ICapabilityProvider {
    val instance = new XpFluidCapa(xpCapa, stack)
    val currentCapa = if (stack.isEmpty) CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY else CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY

    override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean =
      capability == currentCapa


    override def getCapability[T](capability: Capability[T], facing: EnumFacing): T =
      currentCapa.cast(
        if (hasCapability(capability, facing))
          instance
        else
          null
      )
  }

  class XpFluidTankProperties(capa: XpFluidCapa) extends IFluidTankProperties {
    override def getContents: FluidStack = capa.getFluid

    override def getCapacity: Int = capa.getCapacity

    override def canFill: Boolean = true

    override def canDrain: Boolean = true

    override def canFillFluidType(fluidStack: FluidStack): Boolean =
      isValidFluid(fluidStack)

    override def canDrainFluidType(fluidStack: FluidStack): Boolean =
      isValidFluid(fluidStack)
  }

  def isValidFluid(fluidStack: FluidStack) = {
    fluidStack != null && fluidStack.getFluid != null && FluidUtil.areFluidsTheSame(fluidStack.getFluid, Fluids.XP_JUICE.getFluid)
  }

}
