package hohserg.soulkeeper;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Copied from OpenBlocks:
 * https://github.com/OpenMods/OpenModsLib/blob/1.12.X/src/main/java/openmods/utils/EnchantmentUtils.java
 */

public class XPUtils {

    /**
     * Be warned, minecraft doesn't update experienceTotal properly, so we have
     * to do this.
     *
     * @param player
     * @return
     */
    public static int getPlayerXP(EntityPlayer player) {
        return getExperienceForLevelAndBar(player.experienceLevel, player.experience);
    }

    public static int getExperienceForLevelAndBar(int experienceLevel, float experienceBar) {
        return (int) (XPUtils.getExperienceForLevel(experienceLevel) + (experienceBar * xpBarCap(experienceLevel)));
    }

    public static void setPlayerXP(EntityPlayer player, int amount) {
        player.experienceTotal = amount;
        player.experienceLevel = XPUtils.getLevelForExperience(amount);
        int expForLevel = XPUtils.getExperienceForLevel(player.experienceLevel);
        player.experience = (float) (amount - expForLevel) / (float) player.xpBarCap();
    }

    public static void addPlayerXP(EntityPlayer player, int amount) {
        setPlayerXP(player, getPlayerXP(player) + amount);
    }

    public static int xpBarCap(int level) {
        if (level >= 30)
            return 112 + (level - 30) * 9;

        if (level >= 15)
            return 37 + (level - 15) * 5;

        return 7 + level * 2;
    }

    private static int sum(int n, int a0, int d) {
        return n * (2 * a0 + (n - 1) * d) / 2;
    }

    public static int getExperienceForLevel(int level) {
        if (level == 0) return 0;
        if (level <= 15) return sum(level, 7, 2);
        if (level <= 30) return 315 + sum(level - 15, 37, 5);
        return 1395 + sum(level - 30, 112, 9);
    }

    public static int getXpToNextLevel(int level) {
        int levelXP = XPUtils.getExperienceForLevel(level);
        int nextXP = XPUtils.getExperienceForLevel(level + 1);
        return nextXP - levelXP;
    }

    public static int getLevelForExperience(int targetXp) {
        int level = 0;
        while (true) {
            final int xpToNextLevel = xpBarCap(level);
            if (targetXp < xpToNextLevel) return level;
            level++;
            targetXp -= xpToNextLevel;
        }
    }

    public static void main(String[] args) {
        System.out.println("xpBarCap " + xpBarCap(100));
        System.out.println("getXpToNextLevel " + getXpToNextLevel(100));

        System.out.println("getExperienceForLevel " + getExperienceForLevel(100));
        System.out.println("getTotalXPForLevel " + hohserg.soulkeeper.utils.XPUtils.getTotalXPForLevel(100, 0));
    }
}