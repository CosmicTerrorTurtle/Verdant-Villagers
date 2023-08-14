package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.VerticalBlockColumn;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class StructureAccessPoint extends PointOfInterest {

    public String accessPathRoadType;

    /**
     * Creates a new structure access point.
     * @param pos The position of this point (can be relative).
     * @param accessPathRoadType The road type of the access path that will be connected to this point.
     */
    public StructureAccessPoint(BlockPos pos, String accessPathRoadType) {
        super(pos);
        this.accessPathRoadType = accessPathRoadType;
    }

    @Override
    public PointOfInterest copy() {
        return new StructureAccessPoint(new BlockPos(pos), accessPathRoadType);
    }

    /**
     * Creates a new StructureAccessPoint from an NbtCompound.
     * @param nbt The compound representing a StructureAccessPoint.
     */
    public StructureAccessPoint(@NotNull NbtCompound nbt) {
        super(nbt);
        accessPathRoadType = nbt.getString("accessPathRoadType");
    }
    /**
     * Saves this StructureAccessPoint to an NbtCompound.
     * @return The compound representing this StructureAccessPoint.
     */
    @Override
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        nbt.putString("subclass", "StructureAccessPoint");
        nbt.putString("accessPathRoadType", accessPathRoadType);
        return nbt;
    }
}
