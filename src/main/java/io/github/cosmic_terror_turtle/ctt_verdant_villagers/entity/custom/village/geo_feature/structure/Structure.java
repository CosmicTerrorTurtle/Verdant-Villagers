package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.structure;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.data.village.DataRegistry;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.util.NbtUtils;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.geo_feature.GeoFeature;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Structure extends GeoFeature {

    public HashMap<String, HashMap<String, String>> dataPerStructureType;
    protected final BlockPos anchor;
    public ArrayList<PointOfInterest> pointsOfInterest;

    public Structure(Random random, long elementID, BlockPos anchor, StructureTemplate template) {
        super(elementID);
        this.anchor = anchor;
        dataPerStructureType = template.dataPerStructureType;

        setBitsAndPointsOfInterest(template, getRandomRotation(random));
    }

    private void setBitsAndPointsOfInterest(StructureTemplate template, int rotation) {
        setBits(template.getBits(), anchor, rotation);

        pointsOfInterest = new ArrayList<>();
        PointOfInterest newPoint;
        for (PointOfInterest point : template.pointsOfInterest) {
            newPoint = point.copy();
            newPoint.setToAbsolutePositions(anchor, rotation);
            pointsOfInterest.add(newPoint);
        }
    }

    /**
     * Creates a new Structure from an NbtCompound.
     * @param nbt The compound representing a Structure.
     */
    public Structure(@NotNull NbtCompound nbt) {
        super(nbt);

        dataPerStructureType = new HashMap<>();
        NbtCompound structureTypeDataNbt = nbt.getCompound("dataPerStructureType");
        NbtCompound structureTypeDataSubNbt;
        for (String key : structureTypeDataNbt.getKeys()) {
            structureTypeDataSubNbt = structureTypeDataNbt.getCompound(key);
            dataPerStructureType.put(key, new HashMap<>());
            for (String subKey : structureTypeDataSubNbt.getKeys()) {
                dataPerStructureType.get(key).put(subKey, structureTypeDataSubNbt.getString(subKey));
            }
        }

        anchor = NbtUtils.blockPosFromNbt(nbt.getCompound("anchor"));

        pointsOfInterest = new ArrayList<>();
        NbtCompound poisNbt = nbt.getCompound("poi");
        NbtCompound poiNbt;
        for (String key : poisNbt.getKeys()) {
            poiNbt = poisNbt.getCompound(key);
            if (poiNbt.contains("subclass")) {
                pointsOfInterest.add(DataRegistry.getPointOfInterestNbtConstructor(poiNbt.getString("subclass")).apply(poiNbt));
            } else {
                pointsOfInterest.add(new PointOfInterest(poiNbt));
            }
        }
    }
    /**
     * Saves this Structure to an NbtCompound.
     * @return The compound representing this Structure.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = super.toNbt();

        NbtCompound structureTypeDataNbt = new NbtCompound();
        NbtCompound structureTypeDataSubNbt;
        for (Map.Entry<String, HashMap<String, String>> entry : dataPerStructureType.entrySet()) {
            structureTypeDataSubNbt = new NbtCompound();
            for (Map.Entry<String, String> subEntry : entry.getValue().entrySet()) {
                structureTypeDataSubNbt.putString(subEntry.getKey(), subEntry.getValue());
            }
            structureTypeDataNbt.put(entry.getKey(), structureTypeDataSubNbt);
        }
        nbt.put("dataPerStructureType", structureTypeDataNbt);

        nbt.put("anchor", NbtUtils.blockPosToNbt(anchor));

        NbtCompound poisNbt = new NbtCompound();
        for (int i=0; i<pointsOfInterest.size(); i++) {
            poisNbt.put(Integer.toString(i), pointsOfInterest.get(i).toNbt());
        }
        nbt.put("poi", poisNbt);

        return nbt;
    }
}
