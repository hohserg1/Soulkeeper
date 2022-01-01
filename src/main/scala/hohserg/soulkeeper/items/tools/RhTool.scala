package hohserg.soulkeeper.items.tools

import hohserg.soulkeeper.api.ItemXPContainer
import hohserg.soulkeeper.items.{ItemRhinestoneDust, ItemTinyRhinestoneDust}
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.enchantment.{EnchantmentDurability, EnchantmentHelper}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.{Enchantments, Items}
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.util.{EnumHand, NonNullList}
import net.minecraft.world.World

trait RhTool extends ItemXPContainer{
  self: Item =>

  override def getXpCapacity(stack: ItemStack): Int = stack.getMaxDamage

  override def showDurabilityBar(stack: ItemStack): Boolean = false

  override def hasEffect(stack: ItemStack): Boolean = getXp(stack) >= 7 || stack.isItemEnchanted

  def dustAmount: Int

  override def onBlockDestroyed(stack: ItemStack, worldIn: World, state: IBlockState, pos: BlockPos, entityLiving: EntityLivingBase): Boolean = {
    onUseItem(stack, entityLiving)

    true
  }

  override def hitEntity(stack: ItemStack, target: EntityLivingBase, attacker: EntityLivingBase): Boolean = {
    onUseItem(stack, attacker)

    true
  }

  private def onUseItem(stack: ItemStack, entityLiving: EntityLivingBase): Unit =
    if (!entityLiving.world.isRemote) {
      val cur = getXp(stack)
      if (cur == 0 && entityLiving.world.rand.nextInt(15) < entityLiving.world.getLight(entityLiving.getPosition)) {
        if (entityLiving.getHeldItemMainhand == stack)
          entityLiving.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.STICK))

        val residueMaterial = (((stack.getMaxDamage - stack.getItemDamage).toDouble / stack.getMaxDamage) * dustAmount * 4).toInt
        val largePileCount = entityLiving.world.rand.nextInt(Math.min(dustAmount, residueMaterial / 4))
        val tinyPileCount = residueMaterial - largePileCount * 4

        entityLiving.renderBrokenItemStack(stack)
        stack.shrink(1)

        for (_ <- 1 to tinyPileCount)
          dropItem(entityLiving, new ItemStack(ItemTinyRhinestoneDust, 1))

        for (_ <- 1 to largePileCount)
          dropItem(entityLiving, new ItemStack(ItemRhinestoneDust, 1))


      } else {
        val i = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack)
        if (i == 0 || !EnchantmentDurability.negateDamage(stack, i, entityLiving.world.rand))
          setXp(stack, cur - 1)

        if (!EnchantmentDurability.negateDamage(stack, 3, entityLiving.world.rand))
          stack.setItemDamage(stack.getItemDamage + 1)
      }
    }

  private def dropItem(entityLiving: EntityLivingBase, droppedItem: ItemStack): Unit =
    if (!droppedItem.isEmpty) {
      val d0 = entityLiving.posY - 0.30000001192092896D + entityLiving.getEyeHeight.toDouble
      val entityitem = new EntityItem(entityLiving.world, entityLiving.posX, d0, entityLiving.posZ, droppedItem) {
        override def searchForOtherItemsNearby(): Unit =
          if (!cannotPickup)
            super.searchForOtherItemsNearby()
      }
      entityitem.setPickupDelay(20)
      entityitem.setThrower(entityLiving.getName)
      var f2 = 0.3F
      entityitem.motionX = (-MathHelper.sin(entityLiving.rotationYaw * 0.017453292F) * MathHelper.cos(entityLiving.rotationPitch * 0.017453292F) * f2).toDouble
      entityitem.motionZ = (MathHelper.cos(entityLiving.rotationYaw * 0.017453292F) * MathHelper.cos(entityLiving.rotationPitch * 0.017453292F) * f2).toDouble
      entityitem.motionY = (-MathHelper.sin(entityLiving.rotationPitch * 0.017453292F) * f2 + 0.1F).toDouble
      val f3 = entityLiving.world.rand.nextFloat * (Math.PI.toFloat * 2F)
      f2 = 0.08F * entityLiving.world.rand.nextFloat
      entityitem.motionX += Math.cos(f3.toDouble) * f2.toDouble
      entityitem.motionY += ((entityLiving.world.rand.nextFloat - entityLiving.world.rand.nextFloat) * 0.1F).toDouble
      entityitem.motionZ += Math.sin(f3.toDouble) * f2.toDouble

      entityLiving.world.spawnEntity(entityitem)
    }

  override def getSubItems(tab: CreativeTabs, items: NonNullList[ItemStack]): Unit =
    if (tab == getCreativeTab) {
      items.add(new ItemStack(this))
      val stack = new ItemStack(this)
      setXp(stack, getXpCapacity(stack))
      items.add(stack)
    }


}
