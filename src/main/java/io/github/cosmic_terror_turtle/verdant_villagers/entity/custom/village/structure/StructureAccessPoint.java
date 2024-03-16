package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.GeoFeature;
import io.github.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StructureAccessPoint extends PointOfInterest {

    public String accessPathRoadType;
    /**
     * A list of two block positions that determine the lower and upper coordinate tips of the volume.
     */
    public ArrayList<BlockPos> connectionVolume;
    public ArrayList<BlockPos> sidewalkPositions = new ArrayList<>();
    public ArrayList<BlockPos> archPositions = new ArrayList<>();

    /**
     * Creates a new structure access point.
     * @param pos The position of this point (can be relative).
     * @param accessPathRoadType The road type of the access path that will be connected to this point.
     */
    public StructureAccessPoint(BlockPos pos, String accessPathRoadType, ArrayList<BlockPos> connectionVolume,
                                ArrayList<BlockPos> sidewalkPositions, ArrayList<BlockPos> archPositions) {
        super(pos);
        this.accessPathRoadType = accessPathRoadType;
        this.connectionVolume = connectionVolume;
        this.sidewalkPositions.addAll(sidewalkPositions);
        this.archPositions.addAll(archPositions);
    }

    @Override
    public PointOfInterest copy() {
        ArrayList<BlockPos> connectionVolume = new ArrayList<>(this.connectionVolume);
        ArrayList<BlockPos> sidewalkPositions = new ArrayList<>(this.sidewalkPositions);
        ArrayList<BlockPos> archPositions = new ArrayList<>(this.archPositions);
        return new StructureAccessPoint(new BlockPos(pos), accessPathRoadType, connectionVolume,
                sidewalkPositions, archPositions);
    }

    @Override
    public void setToAbsolutePositions(BlockPos anchor, int rotation) {
        super.setToAbsolutePositions(anchor, rotation);
        ArrayList<BlockPos> newConnectionVolume = new ArrayList<>();
        ArrayList<BlockPos> newSidewalkPositions = new ArrayList<>();
        ArrayList<BlockPos> newArchPositions = new ArrayList<>();
        for (BlockPos relPos : this.connectionVolume) {
            newConnectionVolume.add(GeoFeature.rotate(anchor, relPos, rotation));
        }
        for (BlockPos relPos : this.sidewalkPositions) {
            newSidewalkPositions.add(GeoFeature.rotate(anchor, relPos, rotation));
        }
        for (BlockPos relPos : this.archPositions) {
            newArchPositions.add(GeoFeature.rotate(anchor, relPos, rotation));
        }
        this.connectionVolume = newConnectionVolume;
        this.sidewalkPositions = newSidewalkPositions;
        this.archPositions = newArchPositions;
    }

    /**
     * Creates a new StructureAccessPoint from an NbtCompound.
     * @param nbt The compound representing a StructureAccessPoint.
     */
    public StructureAccessPoint(@NotNull NbtCompound nbt) {
        super(nbt);
        accessPathRoadType = nbt.getString("accessPathRoadType");
        NbtCompound connectionVolumeNbt = nbt.getCompound("connectionVolume");
        for (String key : connectionVolumeNbt.getKeys()) {
            connectionVolume.add(NbtUtils.blockPosFromNbt(connectionVolumeNbt.getCompound(key)));
        }
        NbtCompound sidewalkNbt = nbt.getCompound("sidewalk");
        for (String key : sidewalkNbt.getKeys()) {
            sidewalkPositions.add(NbtUtils.blockPosFromNbt(sidewalkNbt.getCompound(key)));
        }
        NbtCompound archNbt = nbt.getCompound("arch");
        for (String key : archNbt.getKeys()) {
            archPositions.add(NbtUtils.blockPosFromNbt(archNbt.getCompound(key)));
        }
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
        int i;
        NbtCompound connectionVolumeNbt = new NbtCompound();
        i=0;
        for (BlockPos pos : connectionVolume) {
            connectionVolumeNbt.put(Integer.toString(i), NbtUtils.blockPosToNbt(pos));
            i++;
        }
        nbt.put("connectionVolume", connectionVolumeNbt);
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
        return nbt;
    }
}
