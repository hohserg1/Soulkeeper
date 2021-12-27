package hohserg.soulkeeper;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import scala.collection.JavaConversions;
import scala.collection.Set;

import java.util.Arrays;

import static java.util.stream.Collectors.toSet;
import static net.minecraft.init.Blocks.*;

@Config(modid = "soulkeeper")
public class Configuration {

    @Config.Name("soulkeeper soil whitelist")
    public static String[] __soulkeeperGenWhitelist = ImmutableSet.of(STONE, COAL_ORE, DIAMOND_ORE, EMERALD_ORE, GOLD_ORE, IRON_ORE, LAPIS_ORE, REDSTONE_ORE)
            .stream().map(i -> i.getRegistryName().toString()).toArray(String[]::new);

    @Config.Name("rhinestone stalactite rocks worldgen whitelist")
    public static String[] __rhinestoneStalactiteGenWhitelist = ImmutableSet.of(STONE, COAL_ORE, DIAMOND_ORE, EMERALD_ORE, GOLD_ORE, IRON_ORE, LAPIS_ORE, REDSTONE_ORE)
            .stream().map(i -> i.getRegistryName().toString()).toArray(String[]::new);

    @Config.Ignore
    public static Set<Block> soulkeeperGenWhitelist =
            JavaConversions.asScalaSet(
                    Arrays.stream(__soulkeeperGenWhitelist)
                            .map(ResourceLocation::new)
                            .map(ForgeRegistries.BLOCKS::getValue)
                            .collect(toSet()));
    @Config.Ignore
    public static Set<Block> rhinestoneStalactiteGenWhitelist =
            JavaConversions.asScalaSet(
                    Arrays.stream(__rhinestoneStalactiteGenWhitelist)
                            .map(ResourceLocation::new)
                            .map(ForgeRegistries.BLOCKS::getValue)
                            .collect(toSet()));

    public static int soulkeeperRarity = 8;

    public static int darkCrystalStalactiteRarity = 11;

    public static double withoutXPGrowChance = 0.1;

    public static int soulkeeperXPDrop = 20;

    @Config.RangeInt(min = 1)
    public static int rhinestoneDustChestLootRarity = 100;
    @Config.RangeInt(min = 1)
    public static int rhinestoneToolsChestLootRarity = 1000;

    @Config.RangeInt(min = 1)
    public static int rhinestoneBottleCapacity = 20;

    @Config.RangeInt(min = 1)
    public static int rhinestoneOrbCapacity = 20000;

}
