package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeature;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeatureBit;
import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RoadFeature extends GeoFeature {

    public ArrayList<BlockPos> sidewalkPositions = new ArrayList<>();
    public ArrayList<BlockPos> archPositions = new ArrayList<>();
    public ArrayList<GeoFeatureBit> pillarStartBits = new ArrayList<>();

    public RoadFeature(int elementID) {
        super(elementID);
    }

    @Override
    public void removeBits(ArrayList<BlockPos> absolutePositions) {
        ArrayList<GeoFeatureBit> toBeRemoved = new ArrayList<>();
        for (GeoFeatureBit bit : bits) {
            if (absolutePositions.contains(bit.blockPos)) {
                toBeRemoved.add(bit);

            }
        }
        for (GeoFeatureBit bit : toBeRemoved) {
            bits.remove(bit);
            sidewalkPositions.remove(bit.blockPos);
            archPositions.remove(bit.blockPos);
            pillarStartBits.removeIf(pillarBit -> pillarBit.blockPos.equals(bit.blockPos));
        }
        updateBounds();
        updateBoundingBoxChunks();
    }

    /**
     * Creates a new RoadFeature from an NbtCompound.
     * @param nbt The compound representing a RoadFeature.
     */
    public RoadFeature(@NotNull NbtCompound nbt) {
        super(nbt);
        NbtCompound sidewalkNbt = nbt.getCompound("sidewalk");
        for (String key : sidewalkNbt.getKeys()) {
            sidewalkPositions.add(NbtUtils.blockPosFromNbt(sidewalkNbt.getCompound(key)));
        }
        NbtCompound archNbt = nbt.getCompound("arch");
        for (String key : archNbt.getKeys()) {
            archPositions.add(NbtUtils.blockPosFromNbt(archNbt.getCompound(key)));
        }
        NbtCompound pillarNbt = nbt.getCompound("pillar");
        for (String key : pillarNbt.getKeys()) {
            pillarStartBits.add(new GeoFeatureBit(pillarNbt.getCompound(key)));
        }
    }

    /**
     * Saves this RoadFeature to an NbtCompound.
     * @return The compound representing this RoadFeature.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();
        int i;
        NbtCompound sidewalkNbt = new NbtCompound();
        i=0;
        for (BlockPos pos : sidewalkPositions) {
            sidewalkNbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(pos));
            i++;
        }
        nbt.put("sidewalk", sidewalkNbt);
        NbtCompound archNbt = new NbtCompound();
        i=0;
        for (BlockPos pos : archPositions) {
            archNbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(pos));
            i++;
        }
        nbt.put("arch", archNbt);
        NbtCompound pillarNbt = new NbtCompound();
        i=0;
        for (GeoFeatureBit bit : pillarStartBits) {
            pillarNbt.put(Integer.toString(i), bit.toNbt());
            i++;
        }
        nbt.put("pillar", pillarNbt);
        return nbt;
    }
}
