package hohserg.soulkeeper.enchantments

import hohserg.soulkeeper.items.tools.{ItemRhAxe, ItemRhSword}
import hohserg.soulkeeper.potions.EffectXPLeak
import hohserg.soulkeeper.{Configuration, Main}
import net.minecraft.enchantment.Enchantment.Rarity
import net.minecraft.enchantment.{Enchantment, EnumEnchantmentType}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.entity.{Entity, EntityLivingBase}
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

@EventBusSubscriber(modid = Main.modid)
object EnchantXPLeak extends Enchantment(Rarity.RARE, EnumEnchantmentType.WEAPON, Array(EntityEquipmentSlot.MAINHAND)) {

  override def getMaxLevel: Int = 3

  override def canApplyAtEnchantingTable(stack: ItemStack): Boolean = stack.getItem == ItemRhSword

  override def canApply(stack: ItemStack): Boolean = stack.getItem == ItemRhSword || stack.getItem == ItemRhAxe


  override def onEntityDamaged(user: EntityLivingBase, target: Entity, level: Int): Unit = {
    target match {
      case targetPlayer: EntityPlayerMP =>
        if (user.world.rand.nextDouble() < Configuration.xpLeakInflictChance)
          targetPlayer.addPotionEffect(new PotionEffect(EffectXPLeak, 20 * 5 * level))
      case _ =>
    }
  }
}
