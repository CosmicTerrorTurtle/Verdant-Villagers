package net.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure;

import net.cosmic_terror_turtle.verdant_villagers.entity.custom.village.VerticalBlockColumn;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class StructureAccessPoint extends PointOfInterest {

    public double radius;
    public VerticalBlockColumn templateRoadColumn;

    /**
     * Creates a new structure access point.
     * @param pos The position of this point (can be relative).
     * @param radius The diameter of the access path that will be connected to this point.
     * @param templateRoadColumn The surface material of the path.
     */
    public StructureAccessPoint(BlockPos pos, double radius, VerticalBlockColumn templateRoadColumn) {
        super(pos);
        this.radius = radius;
        this.templateRoadColumn = templateRoadColumn;
    }

    @Override
    public PointOfInterest copy() {
        return new StructureAccessPoint(new BlockPos(pos), radius, templateRoadColumn);
    }

    /**
     * Creates a new StructureAccessPoint from an NbtCompound.
     * @param nbt The compound representing a StructureAccessPoint.
     */
    public StructureAccessPoint(@NotNull NbtCompound nbt) {
        super(nbt);
        radius = nbt.getDouble("radius");
        templateRoadColumn = new VerticalBlockColumn(nbt.getCompound("templateRoadColumn"));
    }
    /**
     * Saves this StructureAccessPoint to an NbtCompound.
     * @return The compound representing this StructureAccessPoint.
     */
    @Override
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        nbt.putString("subclass", "StructureAccessPoint");
        nbt.putDouble("radius", radius);
        nbt.put("templateRoadColumn", templateRoadColumn.toNbt());
        return nbt;
    }
}
