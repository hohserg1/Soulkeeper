package hohserg.soulkeeper.blocks

import hohserg.soulkeeper.XPUtils
import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.api.crafting.{DummyInfuserRecipe, InfuserRecipe, StepInfuserRecipe}
import hohserg.soulkeeper.items.HasHelp
import javax.annotation.Nonnull
import net.minecraft.block.Block
import net.minecraft.block.material.{MapColor, Material}
import net.minecraft.block.state.{BlockFaceShape, IBlockState}
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World, WorldServer}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.{CapabilityItemHandler, ItemStackHandler}

object BlockInfuser extends Block(Material.ROCK, MapColor.PURPLE) with HasHelp{
  setLightOpacity(0)
  setHardness(5)
  setResistance(2000)

  override def isFullCube(state: IBlockState): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def hasTileEntity(state: IBlockState): Boolean = true

  override def createTileEntity(world: World, state: IBlockState): TileEntity = new TileInfuser

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D)

  override def getBlockFaceShape(worldIn: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing): BlockFaceShape =
    if (face eq EnumFacing.DOWN)
      BlockFaceShape.SOLID
    else
      BlockFaceShape.UNDEFINED


  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!worldIn.isRemote)
      worldIn.getTileEntity(pos) match {
        case tile: TileInfuser =>

          def ejectItem(): Unit = {
            val input = tile.inv.getStackInSlot(0)
            tile.inv.setStackInSlot(0, ItemStack.EMPTY)
            tile.getWorld.spawnEntity(new EntityItem(tile.getWorld, tile.getPos.getX + 0.5, tile.getPos.getY + 0.8, tile.getPos.getZ + 0.5, input.copy()))
          }

          if (tile.inv.getStackInSlot(0).isEmpty) {
            val stackInHand = playerIn.getHeldItem(hand)
            if (InfuserRecipe.findRecipe(stackInHand).isDefined) {
              val input = stackInHand.copy()
              input.setCount(1)
              stackInHand.shrink(1)
              tile.inv.setStackInSlot(0, input)
            }
          } else {
            val input = tile.inv.getStackInSlot(0)
            if (playerIn.isSneaking)
              ejectItem()
            else {
              val maybeRecipe = InfuserRecipe.findRecipe(input)
              maybeRecipe.foreach {
                case r: DummyInfuserRecipe =>
                  val playerXP = XPUtils.getPlayerXP(playerIn)
                  if (playerXP >= r.xp) {
                    XPUtils.setPlayerXP(playerIn, playerXP - r.xp)
                    tile.inv.setStackInSlot(0, r.output.copy())
                  } else
                    ejectItem()

                case r: StepInfuserRecipe =>
                  val capa = CapabilityXPContainer(input)
                  if (capa != null) {
                    val curXP = capa.getXp
                    val playerXP = XPUtils.getPlayerXP(playerIn)

                    if (curXP >= capa.getXpCapacity || curXP >= r.stepXP * r.stepAmount || playerXP < r.stepXP)
                      ejectItem()

                    else if (curXP < capa.getXpCapacity && playerXP >= r.stepXP) {
                      XPUtils.setPlayerXP(playerIn, playerXP - r.stepXP)
                      capa.setXp(curXP + r.stepXP)
                      tile.sendUpdates()
                    }

                  }
              }

              if (maybeRecipe.isEmpty) {
                tile.inv.setStackInSlot(0, ItemStack.EMPTY)
                tile.getWorld.spawnEntity(new EntityItem(tile.getWorld, tile.getPos.getX + 0.5, tile.getPos.getY + 0.8, tile.getPos.getZ + 0.5, input.copy()))
              }
            }
          }
        case _ =>
      }
    true
  }

  class TileInfuser extends TileEntity {
    val inv = new ItemStackHandler(2) {
      override def onContentsChanged(slot: Int): Unit =
        sendUpdates()
    }

    override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
      val r = super.writeToNBT(compound)
      r.setTag("inv", inv.serializeNBT())
      r
    }

    override def readFromNBT(compound: NBTTagCompound): Unit = {
      super.readFromNBT(compound)
      inv.deserializeNBT(compound.getCompoundTag("inv"))
    }

    override def getUpdateTag: NBTTagCompound = writeToNBT(new NBTTagCompound)

    override def onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity): Unit =
      readFromNBT(pkt.getNbtCompound)

    override def getUpdatePacket: SPacketUpdateTileEntity =
      new SPacketUpdateTileEntity(pos, 3, getUpdateTag)

    override def hasCapability(@Nonnull cap: Capability[_], side: EnumFacing): Boolean =
      cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(cap, side)

    override def getCapability[T](@Nonnull cap: Capability[T], side: EnumFacing): T =
      if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inv)
      else
        super.getCapability(cap, side)

    def sendUpdates(): Unit =
      world match {
        case server: WorldServer =>
          val chunk = server.getPlayerChunkMap.getEntry(pos.getX >> 4, pos.getZ >> 4)
          if (chunk != null) chunk.sendPacket(getUpdatePacket)
        case _ =>
      }


  }

}
