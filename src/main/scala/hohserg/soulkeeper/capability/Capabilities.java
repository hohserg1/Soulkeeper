package hohserg.soulkeeper.capability;

import hohserg.soulkeeper.capability.chunk.ExpInChunk;
import hohserg.soulkeeper.capability.player.ExpInPlayer;
import hohserg.soulkeeper.capability.tile.PrevLootTable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities {
    @CapabilityInject(ExpInChunk.class)
    public static Capability<ExpInChunk> expInChunk;

    @CapabilityInject(ExpInPlayer.class)
    public static Capability<ExpInPlayer> expInPlayer;

    @CapabilityInject(PrevLootTable.class)
    public static Capability<PrevLootTable> prevLootTable;
}
