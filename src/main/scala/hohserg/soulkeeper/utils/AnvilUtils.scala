package hohserg.soulkeeper.utils

import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.{ContainerRepair, IInventory}
import net.minecraft.item.ItemStack
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.common.util.FakePlayerFactory
import net.minecraftforge.event.AnvilUpdateEvent

object AnvilUtils {

  case class AnvilRecipe(xpCost: Int, left: ItemStack, right: ItemStack, result: ItemStack)

  def getActualAnvilRecipe(e: AnvilUpdateEvent): Option[AnvilRecipe] =
    if (!handlingFlag) {
      if (e.getOutput.isEmpty) {
        calculateVanillaAnvilRecipe(e)
      } else
        Some(moddedAnvilRecipe(e))
    } else
      None


  private def moddedAnvilRecipe(e: AnvilUpdateEvent): AnvilRecipe = {
    val left = e.getLeft.copy()
    left.setCount(1)
    val right = e.getRight.copy()
    right.setCount(e.getMaterialCost)
    AnvilRecipe(e.getCost, left, right, e.getOutput.copy())
  }

  private lazy val virtualAnvil = new ContainerRepair(
    new InventoryPlayer(FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0))),
    DimensionManager.getWorld(0),
    FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0))
  )

  private var handlingFlag = false

  private def calculateVanillaAnvilRecipe(e: AnvilUpdateEvent): Option[AnvilRecipe] = {
    handlingFlag = true

    virtualAnvil.inputSlots.setInventorySlotContents(0, e.getLeft)
    virtualAnvil.inputSlots.setInventorySlotContents(1, e.getRight)
    virtualAnvil.updateRepairOutput()

    val left = e.getLeft.copy()
    left.setCount(1)
    val right = e.getRight.copy()
    right.setCount(virtualAnvil.materialCost)
    val result = virtualAnvil.outputSlot.getStackInSlot(0).copy()

    handlingFlag = false

    if (result.isEmpty)
      None
    else
      Some(AnvilRecipe(virtualAnvil.maximumCost, left, right, result))
  }

}
