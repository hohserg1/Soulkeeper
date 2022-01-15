package hohserg.soulkeeper.items

import hohserg.soulkeeper.capability.chunk.ExpInChunkProvider
import hohserg.soulkeeper.{Main, XPUtils}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.{ActionResult, EnumHand, NonNullList}
import net.minecraft.world.World

object ItemDebugXPMeter extends Item {

  override def onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult[ItemStack] = {
    if (!worldIn.isRemote) {
      playerIn.sendMessage(new TextComponentString("Chunk xp: " + ExpInChunkProvider.getCapability(worldIn, playerIn.getPosition).experience))
      playerIn.sendMessage(new TextComponentString("Player xp: " + XPUtils.getPlayerXP(playerIn)))
    }
    super.onItemRightClick(worldIn, playerIn, handIn)
  }

  override def getSubItems(tab: CreativeTabs, items: NonNullList[ItemStack]): Unit =
    if (Main.debugMode)
      super.getSubItems(tab, items)

}
