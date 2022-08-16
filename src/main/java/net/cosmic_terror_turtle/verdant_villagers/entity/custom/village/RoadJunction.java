package net.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import net.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RoadJunction extends GeoFeature {

    public BlockPos pos;
    public double radius;
    public double sameHeightRadius;

    public RoadJunction(int elementID, BlockPos pos, double radius, double sameHeightRadius) {
        super(elementID);
        this.pos = pos;
        this.radius = radius;
        this.sameHeightRadius = sameHeightRadius;
    }
    public RoadJunction(int elementID, BlockPos pos, RoadType type) {
        super(elementID);
        this.pos = pos;
        radius = type.junctionRadius;
        sameHeightRadius = type.junctionSameHeightRadius;

        setBitsAndMegaBlocks(type.junctionBlockColumnRadii, type.junctionTemplateBlockColumns);
    }

    private void setBitsAndMegaBlocks(double[] blockColumnRadii, VerticalBlockColumn[] templateBlockColumns) {
        ArrayList<GeoFeatureBit> relativeBits = new ArrayList<>();
        BlockPos position;
        VerticalBlockColumn templateColumn;
        for (int x = (int) -radius; x<=radius; x++) {
            for (int z = (int) -radius; z<=radius; z++) {
                if (x*x+z*z <= radius*radius) {
                    position = new BlockPos(x, 0, z);
                    templateColumn = templateBlockColumns[0];
                    for (int i = 0; i< blockColumnRadii.length; i++) {
                        if (x*x+z*z <= blockColumnRadii[i]* blockColumnRadii[i]) {
                            templateColumn = templateBlockColumns[i];
                            break;
                        }
                    }
                    for (int i=0; i<templateColumn.states.length; i++) {
                        relativeBits.add(new GeoFeatureBit(templateColumn.states[i], position.up(i-templateColumn.baseLevelIndex)));
                    }
                }
            }
        }
        setBits(relativeBits, pos, ROTATE_NOT);
    }

    /**
     * Creates a new RoadJunction from an NbtCompound.
     * @param nbt The compound representing a RoadJunction.
     */
    public RoadJunction(@NotNull NbtCompound nbt) {
        super(nbt);
        pos = NbtUtils.blockPosFromNbt(nbt.getCompound("pos"));
        radius = nbt.getDouble("radius");
        sameHeightRadius = nbt.getDouble("sameHeightRadius");
    }
    /**
     * Saves this RoadJunction to an NbtCompound.
     * @return The compound representing this RoadJunction.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        nbt.put("pos", NbtUtils.blockPosToNbt(pos));
        nbt.putDouble("radius", radius);
        nbt.putDouble("sameHeightRadius", sameHeightRadius);
        return nbt;
    }
}
