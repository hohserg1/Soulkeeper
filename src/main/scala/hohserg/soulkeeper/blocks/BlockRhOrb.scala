package hohserg.soulkeeper.blocks

import hohserg.soulkeeper.api.CapabilityXPContainer
import hohserg.soulkeeper.capability.tile.XpFluidCapa
import hohserg.soulkeeper.items.HasHelp
import hohserg.soulkeeper.network.PacketTypes.ChangeRhOrbStep
import hohserg.soulkeeper.{Configuration, Main, XPUtils}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, SoundType}
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.init.Items
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.NetworkManager
import net.minecraft.network.play.server.SPacketUpdateTileEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util._
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, RayTraceResult}
import net.minecraft.world.{IBlockAccess, World, WorldServer}
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.mutable

@EventBusSubscriber(modid = Main.modid)
object BlockRhOrb extends Block(Material.GLASS) with RhColor with HasHelp {

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


  val player2Step = new mutable.HashMap[String, InteractStep]

  def onChangeStep(entityPlayerMP: EntityPlayerMP, id: Int): Unit = {
    player2Step += entityPlayerMP.getName -> stepId.applyOrElse(id, (_: Int) => By1)
  }

  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (!worldIn.isRemote) {
      worldIn.getTileEntity(pos) match {
        case tile: TileRhOrb =>
          val playerXP = XPUtils.getPlayerXP(player)

          val step = player2Step.getOrElseUpdate(player.getName, By1)

          if (player.isSneaking) {
            val amount =
              Math.min(
                tile.xp,
                step match {
                  case By1 =>
                    1
                  case By10 =>
                    10
                  case By100 =>
                    100
                  case All =>
                    tile.xp
                }
              )

            if (tile.xp >= amount) {
              tile.xp -= amount
              XPUtils.setPlayerXP(player, playerXP + amount)
              tile.sendUpdates()
            }
          } else {
            val amount =
              Math.min(
                playerXP,
                step match {
                  case By1 =>
                    1
                  case By10 =>
                    10
                  case By100 =>
                    100
                  case All =>
                    playerXP
                }
              )

            if (playerXP >= amount && tile.xp + amount <= Configuration.rhinestoneOrbCapacity) {
              XPUtils.setPlayerXP(player, playerXP - amount)
              tile.xp += amount
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
      addCapa(event, new CapabilityXPContainer {
        override def getXp: Int = stack.getOrCreateSubCompound("xp_container").getInteger("xp")

        override def setXp(amount: Int): Unit = stack.getOrCreateSubCompound("xp_container").setInteger("xp", Math.max(0, Math.min(amount, getXpCapacity)))

        override def getXpCapacity: Int = Configuration.rhinestoneOrbCapacity
      }, stack)
    }
  }

  @SubscribeEvent
  def attachCapaToTile(event: AttachCapabilitiesEvent[TileEntity]): Unit = {
    event.getObject match {
      case tile: TileRhOrb =>
        addCapa(event, new CapabilityXPContainer {
          override def getXpCapacity: Int = Configuration.rhinestoneOrbCapacity

          override def getXp: Int = tile.xp

          override def setXp(amount: Int): Unit = {
            tile.xp = amount
            tile.sendUpdates()
          }
        }, ItemStack.EMPTY)
      case _ =>
    }
  }

  def addCapa(event: AttachCapabilitiesEvent[_], xpCapa: CapabilityXPContainer, stack: ItemStack): Unit = {
    event.addCapability(new ResourceLocation(Main.modid, "capa_xp_container"), xpCapa)

    if (Loader.isModLoaded("enderio"))
      event.addCapability(new ResourceLocation(Main.modid, "capa_xp_fluid"), new XpFluidCapa.Provider(xpCapa, stack))
  }

  sealed trait InteractStep {
    def prev: InteractStep

    def next: InteractStep
  }

  case object By1 extends InteractStep {
    override def prev: InteractStep = All

    override def next: InteractStep = By10

    override def toString: String = "by 1 xp"
  }

  case object By10 extends InteractStep {
    override def prev: InteractStep = By1

    override def next: InteractStep = By100

    override def toString: String = "by 10 xp"
  }

  case object By100 extends InteractStep {
    override def prev: InteractStep = By10

    override def next: InteractStep = All

    override def toString: String = "by 100 xp"
  }

  case object All extends InteractStep {
    override def prev: InteractStep = By100

    override def next: InteractStep = By1

    override def toString: String = "by all xp"
  }

  val stepId = Seq(By1, By10, By100, All)

  var currentStep: InteractStep = By1
  var prevSlot = -1

  @SubscribeEvent
  def rollStep(event: PlayerTickEvent): Unit = {
    val player = event.player
    val world = player.world
    if (world.isRemote) {
      val result = Minecraft.getMinecraft.objectMouseOver
      if (result.typeOfHit == RayTraceResult.Type.BLOCK && world.getBlockState(result.getBlockPos).getBlock == this) {

        if (prevSlot == -1)
          prevSlot = player.inventory.currentItem

        val roll = player.inventory.currentItem - prevSlot

        val nextStep =
          if (roll > 0) {
            (1 to roll).foldLeft(currentStep) { case (r, _) => r.next }
          } else if (roll < 0) {
            (1 to -roll).foldLeft(currentStep) { case (r, _) => r.prev }
          } else
            currentStep

        if (currentStep != nextStep) {
          currentStep = nextStep
          player.inventory.currentItem = prevSlot
          ChangeRhOrbStep.packet().writeInt(stepId.indexOf(currentStep)).sendToServer()
        }
      } else
        prevSlot = -1
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

    def sendUpdates(): Unit =
      world match {
        case server: WorldServer =>
          val chunk = server.getPlayerChunkMap.getEntry(pos.getX >> 4, pos.getZ >> 4)
          if (chunk != null) chunk.sendPacket(getUpdatePacket)
        case _ =>
      }

  }

}
