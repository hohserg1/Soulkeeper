package hohserg.soulkeeper.entities

import hohserg.soulkeeper.blocks.BlockDarkRhinestonePowder
import hohserg.soulkeeper.capability.chunk.ExpInChunkProvider
import hohserg.soulkeeper.network.PacketTypes
import io.netty.buffer.ByteBuf
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData

class CustomEntityXPOrb(world: World) extends EntityXPOrb(world) with IEntityAdditionalSpawnData {
  def this(xp: Int, world: World, x: Double, y: Double, z: Double) = {
    this(world)
    setSize(0.5F, 0.5F)
    setPosition(x, y, z)
    xpValue = xp
  }

  def this(base: EntityXPOrb) = {
    this(base.xpValue, base.world, base.posX, base.posY, base.posZ)
    this.rotationYaw = base.rotationYaw
    this.motionX = base.motionX
    this.motionY = base.motionY
    this.motionZ = base.motionZ
  }

  var consumerBlacklist: Set[String] = Set()

  override def onCollideWithPlayer(entityIn: EntityPlayer): Unit =
    if (entityIn.world.isRemote || !consumerBlacklist.contains(entityIn.getName))
      super.onCollideWithPlayer(entityIn)

  def sync(): Unit = {
    if (!world.isRemote)
      PacketTypes.SyncXPOrb.packet()
        .writeInt(getEntityId)
        .writeInt(xpValue)
        .writeInt(xpOrbAge)
        .sendPacketToAllAround(posX, posY, posZ, 256, world.provider.getDimension)
  }


  def setXpValue(v: Int): Unit = {
    xpValue = v
    if (!world.isRemote)
      sync()
  }

  def setAge(v: Int): Unit = {
    xpOrbAge = v
    if (!world.isRemote)
      sync()
  }

  private var halfOnFallingCooldown = 0

  override def fall(distance: Float, damageMultiplier: Float): Unit = {
    super.fall(distance, damageMultiplier)

    if (!world.isRemote) {

      if (world.getBlockState(underPos).getBlock != BlockDarkRhinestonePowder)
        if (halfOnFallingCooldown == 0) {
          halfOnFallingCooldown = 20 * 10
          halfXPWithChunk()
        }
    }
  }

  private def underPos = getPosition.down()

  def mergeNearOrbs(): Unit = {
    val orbs = world.getEntitiesWithinAABB(classOf[CustomEntityXPOrb], this.getEntityBoundingBox.grow(1, 1, 1))
    for (i <- 0 until orbs.size) {
      val o = orbs.get(i)
      if (!(o eq this)) {
        if (o.consumerBlacklist == this.consumerBlacklist)
          if (o.isEntityAlive) {
            xpValue += o.xpValue
            xpOrbAge = math.min(xpOrbAge, o.xpOrbAge)
            o.setDead()
          }
      }
    }
    if (orbs.size > 0)
      sync()
  }

  override def onUpdate(): Unit = {
    if (!world.isRemote) {
      if (xpOrbAge >= 5999) {
        val underState = world.getBlockState(underPos)
        if (underState.getBlock == BlockDarkRhinestonePowder && underState.getValue(BlockDarkRhinestonePowder.infuseProperty) == 15) {
          setAge(0)
        } else {
          if (xpValue >= 73) {
            halfXPWithChunk()
            setAge(1200)
          } else {
            ExpInChunkProvider.getCapability(world, getPosition).experience += xpValue
            xpValue = 0
          }
        }
      } else {
        if (ticksExisted % (20 * 30) == 0)
          mergeNearOrbs()
      }

      if (halfOnFallingCooldown > 0)
        halfOnFallingCooldown -= 1
    }

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
