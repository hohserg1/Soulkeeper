package hohserg.soulkeeper.entities

import hohserg.soulkeeper.blocks.BlockDarkRhinestone
import hohserg.soulkeeper.capability.ExpInChunkProvider
import hohserg.soulkeeper.network.PacketTypes
import io.netty.buffer.ByteBuf
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData

class CustomEntityXPOrb(world: World) extends EntityXPOrb(world) with IEntityAdditionalSpawnData {
  def this(base: EntityXPOrb) = {
    this(base.world)
    this.setSize(0.5F, 0.5F)
    this.setPosition(base.posX, base.posY, base.posZ)
    this.rotationYaw = base.rotationYaw
    this.motionX = base.motionX
    this.motionY = base.motionY
    this.motionZ = base.motionZ
    this.xpValue = base.xpValue
  }

  def setXpValue(v: Int): Unit = {
    xpValue = v
    if (!world.isRemote)
      PacketTypes.SyncXPOrb.packet().writeInt(getEntityId).writeInt(v).sendToDimension(world.provider.getDimension)
  }


  override def doBlockCollisions(): Unit = super.doBlockCollisions()

  override def fall(distance: Float, damageMultiplier: Float): Unit = {
    super.fall(distance, damageMultiplier)
    //if (world.getBlockState(underPos).getBlock == BlockDarkRhinestonePowder)
    //  BlockDarkRhinestonePowder.infuse(world, underPos, this)
    if (world.getBlockState(underPos).getBlock != BlockDarkRhinestone)
      halfXPWithChunk()
  }

  private def underPos = getPosition.down()

  override def onUpdate(): Unit = {
    if (xpOrbAge >= 6000) {
      if (world.getBlockState(underPos).getBlock != BlockDarkRhinestone) {
        if (xpValue >= 73) {
          halfXPWithChunk()
          xpOrbAge = 1200
        } else
          ExpInChunkProvider.getCapability(world, getPosition).experience += xpValue
      } else
        xpOrbAge = 0
    }

    /*
    if (xpValue <= 0)
      setDead()

    //if (xpOrbAge == 1000)
      world.getEntitiesWithinAABB(classOf[CustomEntityXPOrb], new AxisAlignedBB(getPosition)).asScala.foreach(e =>
        if (e.xpOrbAge > this.xpOrbAge) {
          setXpValue(this.xpValue + e.xpValue)
          e.setXpValue(0)
        }
      )*/

    super.onUpdate()
  }

  private def halfXPWithChunk(): Unit = {
    val cur = xpValue
    val half1 = cur / 2
    val half2 = cur - half1
    setXpValue(half2)
    ExpInChunkProvider.getCapability(world, getPosition).experience += half1
  }

  override def writeSpawnData(buffer: ByteBuf): Unit = buffer.writeInt(xpValue)

  override def readSpawnData(additionalData: ByteBuf): Unit = xpValue = additionalData.readInt()
}
