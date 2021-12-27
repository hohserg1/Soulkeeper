package hohserg.soulkeeper.items.bottle

import java.util

import hohserg.soulkeeper.{Configuration, XPUtils}
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{EnumAction, Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand}
import net.minecraft.world.World

object ItemFilledBottle extends Item {
  setMaxStackSize(1)

  override def hasEffect(stack: ItemStack): Boolean = true

  val xpTagKey = "xpAmount"

  def stackWithAmount(xpAmount: Int): ItemStack = {
    val r = new ItemStack(ItemFilledBottle)
    r.setTagCompound(new NBTTagCompound)
    r.getTagCompound.setInteger(xpTagKey, xpAmount)
    r
  }

  override def getMaxItemUseDuration(stack: ItemStack) = 16

  override def getItemUseAction(stack: ItemStack) = EnumAction.DRINK

  override def onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult[ItemStack] = {
    playerIn.setActiveHand(handIn)
    new ActionResult[ItemStack](EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn))
  }

  override def onItemUseFinish(stack: ItemStack, worldIn: World, entityLiving: EntityLivingBase): ItemStack = {
    entityLiving match {
      case player: EntityPlayer =>
        XPUtils.addPlayerXP(player, getStoredXP(stack))
        stack.shrink(1)

        new ItemStack(ItemEmptyBottle)

      case _ =>
        stack
    }
  }

  override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[String], flagIn: ITooltipFlag): Unit =
    tooltip.add(I18n.format("soulkeeper.xp") + " " + getStoredXP(stack))


  def getStoredXP(stack: ItemStack): Int =
    if (stack.hasTagCompound && stack.getTagCompound.hasKey(xpTagKey))
      stack.getTagCompound.getInteger(xpTagKey)
    else
      Configuration.rhinestoneBottleCapacity

}
