package hohserg.soulkeeper.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities {
    @CapabilityInject(ExpInChunk.class)
    public static Capability<ExpInChunk> expInChunk;
}
