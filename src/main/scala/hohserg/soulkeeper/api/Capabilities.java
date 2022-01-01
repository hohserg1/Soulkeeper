package hohserg.soulkeeper.api;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class Capabilities {
    @CapabilityInject(CapabilityXPContainer.class)
    public static Capability<CapabilityXPContainer> CAPABILITY_XP_CONTAINER;
}
