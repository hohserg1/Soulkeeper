package hohserg.soulkeeper.blocks

import hohserg.soulkeeper.api.{Capabilities, CapabilityXPContainer}
import hohserg.soulkeeper.{Configuration, Main, XPUtils}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, SoundType}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util._
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, RayTraceResult}
import net.minecraft.world.{IBlockAccess, World, WorldServer}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@EventBusSubscriber(modid = Main.modid)
object BlockRhOrb extends Block(Material.GLASS) with RhColor {
  setHardness(1)
  setResistance(10)
  setSoundType(SoundType.GLASS)
  setHarvestLevel("pickaxe", 0)

  override def isFullBlock(state: IBlockState): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false

  @SideOnly(Side.CLIENT)
  override def getBlockLayer = BlockRenderLayer.TRANSLUCENT

  override def hasTileEntity(state: IBlockState): Boolean = true

  override def createTileEntity(world: World, state: IBlockState): TileEntity = new TileRhOrb

  val pixel = 1d / 16

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
    new AxisAlignedBB(pixel, 0, pixel, 1d - pixel, 1d - pixel * 2, 1d - pixel)

  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!worldIn.isRemote) {
      worldIn.getTileEntity(pos) match {
        case tile: TileRhOrb =>
          val playerXP = XPUtils.getPlayerXP(player)
          if (player.isSneaking) {
            if (tile.xp > 0) {
              tile.xp -= 1
              XPUtils.setPlayerXP(player, playerXP + 1)
              tile.sendUpdates()
            }
          } else {
            if (playerXP > 0 && tile.xp < Configuration.rhinestoneOrbCapacity) {
              XPUtils.setPlayerXP(player, playerXP - 1)
              tile.xp += 1
              tile.sendUpdates()
            }
          }
        case _ =>
      }
    }
    true
  }

  @SubscribeEvent
  def attachCapaToItem(event: AttachCapabilitiesEvent[ItemStack]): Unit = {
    val stack = event.getObject
    if (Item.getItemFromBlock(this) != Items.AIR && stack.getItem == Item.getItemFromBlock(this)) {
      event.addCapability(new ResourceLocation(Main.modid, "capa_xp_container"), new CapabilityXPContainer {
        override def getXp: Int = stack.getOrCreateSubCompound("xp_container").getInteger("xp")

        override def setXp(amount: Int): Unit = stack.getOrCreateSubCompound("xp_container").setInteger("xp", Math.max(0, Math.min(amount, getXpCapacity)))

        override def getXpCapacity: Int = Configuration.rhinestoneOrbCapacity
      })
    }
  }

  def getItemStack(world: IBlockAccess, pos: BlockPos): ItemStack = {
    val r = new ItemStack(this)
    world.getTileEntity(pos) match {
      case tile: TileRhOrb =>
        CapabilityXPContainer(r).setXp(tile.xp)
      case _ =>
        println("te miss")
    }
    r
  }

  override def breakBlock(worldIn: World, pos: BlockPos, state: IBlockState): Unit = {
    Block.spawnAsEntity(worldIn, pos, getItemStack(worldIn, pos))

    super.breakBlock(worldIn, pos, state)
  }

  override def getDrops(drops: NonNullList[ItemStack], world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int): Unit = {
  }

  override def getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack =
    getItemStack(world, pos)

  override def onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = {
    world.getTileEntity(pos) match {
      case tile: TileRhOrb =>
        tile.xp = CapabilityXPContainer(stack).getXp
      case _ =>
    }
  }

  class TileRhOrb extends TileEntity {
    var xp = 0

    override def readFromNBT(compound: NBTTagCompound): Unit = {
      super.readFromNBT(compound)
      xp = compound.getInteger("xp")
    }

    override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
      val r = super.writeToNBT(compound)
      r.setInteger("xp", xp)
      r
    }

    override def getUpdateTag: NBTTagCompound = writeToNBT(new NBTTagCompound)

    override def onDataPacket(net: NetworkManager, pkt: SPacketUpdateTileEntity): Unit =
      readFromNBT(pkt.getNbtCompound)

    override def getUpdatePacket: SPacketUpdateTileEntity =
      new SPacketUpdateTileEntity(pos, 3, getUpdateTag)

    override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean =
      capability == Capabilities.CAPABILITY_XP_CONTAINER || super.hasCapability(capability, facing)

    override def getCapability[T](capability: Capability[T], facing: EnumFacing): T =
      if (capability == Capabilities.CAPABILITY_XP_CONTAINER)
        Capabilities.CAPABILITY_XP_CONTAINER.cast(
          new CapabilityXPContainer {
            override def getXpCapacity: Int = Configuration.rhinestoneOrbCapacity

            override def getXp: Int = xp

            override def setXp(amount: Int): Unit = xp = amount
          }
        )
      else
        super.getCapability(capability, facing)

    def sendUpdates(): Unit =
      world match {
        case server: WorldServer =>
          val chunk = server.getPlayerChunkMap.getEntry(pos.getX >> 4, pos.getZ >> 4)
          if (chunk != null) chunk.sendPacket(getUpdatePacket)
        case _ =>
      }

  }

}
