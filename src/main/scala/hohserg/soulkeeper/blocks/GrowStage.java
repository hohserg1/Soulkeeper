package hohserg.soulkeeper.blocks;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Random;

public enum GrowStage implements IStringSerializable {
    Empty(new AxisAlignedBB(0.15F, 0.0F, 0.15F, 0.85F, 0.92F, 0.85F), 2, 3),
    Ripening(new AxisAlignedBB(0.15F, 0.0F, 0.15F, 0.9F, 1.0F, 0.9F), 4, 3),
    Ripe(new AxisAlignedBB(0.15F, 0.0F, 0.15F, 0.9F, 1.0F, 0.9F), 4, 4);

    public final AxisAlignedBB boundingBox;
    public final int baseDropCount, additionalDropCount;

    @Override
    public String getName() {
        return name().toLowerCase();
    }

    GrowStage(AxisAlignedBB boundingBox, int baseDropCount, int additionalDropCount) {
        this.boundingBox = boundingBox;
        this.baseDropCount = baseDropCount;
        this.additionalDropCount = additionalDropCount;
    }
}
