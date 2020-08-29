package hohserg.soulkeeper.items

import hohserg.soulkeeper.XPUtils
import hohserg.soulkeeper.capability.ExpInChunkProvider
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.{ActionResult, EnumHand}
import net.minecraft.world.World

object ItemDebugXPMeter extends Item {

  override def onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult[ItemStack] = {
    if (!worldIn.isRemote) {
      playerIn.sendMessage(new TextComponentString("Chunk xp: " + ExpInChunkProvider.getCapability(worldIn, playerIn.getPosition).experience))
      playerIn.sendMessage(new TextComponentString("Player xp: " + XPUtils.getPlayerXP(playerIn)))
    }
    super.onItemRightClick(worldIn, playerIn, handIn)
  }

}
