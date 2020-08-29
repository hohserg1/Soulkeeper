package hohserg.soulkeeper.network;

import codechicken.lib.packet.PacketCustom;
import hohserg.soulkeeper.Main;

public enum PacketTypes {
    SyncXPOrb;

    public PacketCustom packet() {
        return new PacketCustom(Main.modid(), ordinal() + 1);
    }

    public static PacketTypes fromInt(int id) {
        return values()[id - 1];
    }
}
