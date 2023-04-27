package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeature;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeatureBit;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.VerticalBlockColumn;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadType;
import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RoadJunction extends GeoFeature {

    public BlockPos pos;
    public ArrayList<BlockPos> sidewalkPositions = new ArrayList<>();
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
                        // Add bit.
                        GeoFeatureBit bit = new GeoFeatureBit(templateColumn.states[i], position.up(i-templateColumn.baseLevelIndex));
                        relativeBits.add(bit);
                        // If the bit is part of the sidewalk (int = 1 for that index), add its position.
                        if (templateColumn.ints[i] == 1) {
                            sidewalkPositions.add(pos.add(bit.blockPos));
                        }
                    }
                }
            }
        }
        setBits(relativeBits, pos, ROTATE_NOT);
    }

    /**
     * Tests if a {@link BlockPos} is part of this {@link RoadJunction}'s sidewalk.
     * @param pos The {@link BlockPos} to test.
     * @return True if {@code pos} is a sidewalk position.
     */
    public boolean positionIsSidewalk(BlockPos pos) {
        for (BlockPos sidewalkPos : sidewalkPositions) {
            if (pos.equals(sidewalkPos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new RoadJunction from an NbtCompound.
     * @param nbt The compound representing a RoadJunction.
     */
    public RoadJunction(@NotNull NbtCompound nbt) {
        super(nbt);
        pos = NbtUtils.blockPosFromNbt(nbt.getCompound("pos"));
        NbtCompound sidewalkNbt = nbt.getCompound("sidewalk");
        for (String key : sidewalkNbt.getKeys()) {
            sidewalkPositions.add(NbtUtils.blockPosFromNbt(sidewalkNbt.getCompound(key)));
        }
        radius = nbt.getDouble("radius");
        sameHeightRadius = nbt.getDouble("sameHeightRadius");
    }
    /**
     * Saves this RoadJunction to an NbtCompound.
     * @return The compound representing this RoadJunction.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        int i;
        nbt.put("pos", NbtUtils.blockPosToNbt(pos));
        NbtCompound sidewalkNbt = new NbtCompound();
        i=0;
        for (BlockPos pos : sidewalkPositions) {
            sidewalkNbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(pos));
            i++;
        }
        nbt.put("sidewalk", sidewalkNbt);
        nbt.putDouble("radius", radius);
        nbt.putDouble("sameHeightRadius", sameHeightRadius);
        return nbt;
    }
}
