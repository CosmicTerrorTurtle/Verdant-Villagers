package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadDot;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadEdge;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadJunction;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.Structure;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.structure.StructureAccessPoint;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class GeoFeatureCollision {

    /**
     * Determines if the features overlap.
     * @param feature1 First input.
     * @param feature2 Second input.
     * @return True if they overlap.
     */
    static boolean featuresOverlap(GeoFeature feature1, GeoFeature feature2) {
        // Check bounds.
        if (!feature1.boundsCollideWith(feature2)) {
            return false;
        }
        // Check the bounding box chunks 16 for collision.
        boolean chunksOverlap = false;
        for (BlockPos feature1LowerTip : feature1.getBoundingBoxChunks16()) {
            for (BlockPos feature2LowerTip : feature2.getBoundingBoxChunks16()) {
                if (feature1LowerTip.equals(feature2LowerTip)) {
                    chunksOverlap = true;
                    break;
                }
            }
        }
        if (!chunksOverlap) return false;
        // Check the bounding box chunks 4 for collision.
        chunksOverlap = false;
        for (BlockPos feature1LowerTip : feature1.getBoundingBoxChunks4()) {
            for (BlockPos feature2LowerTip : feature2.getBoundingBoxChunks4()) {
                if (feature1LowerTip.equals(feature2LowerTip)) {
                    chunksOverlap = true;
                    break;
                }
            }
        }
        if (!chunksOverlap) return false;
        // Check the bits for collision.
        for (GeoFeatureBit feature1Bit : feature1.getBits()) {
            for (GeoFeatureBit feature2Bit : feature2.getBits()) {
                if (feature1Bit.blockPos.equals(feature2Bit.blockPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether two edges overlap, but ignores block positions that are within the same height radius of
     * the junctions that the edges share. Those overlaps are then removed from {@code newEdge}.
     * @param newEdge The first edge.
     * @param oldEdge The already existing Edge.
     * @return True if the edges overlap.
     */
    static boolean edgesOverlap(RoadEdge newEdge, RoadEdge oldEdge) {
        // Check bounds.
        if (!newEdge.boundsCollideWith(oldEdge)) {
            return false;
        }
        // Determine shared junctions.
        ArrayList<RoadJunction> sharedJunctions = new ArrayList<>();
        if (newEdge.from.elementID == oldEdge.from.elementID || newEdge.from.elementID == oldEdge.to.elementID) {
            sharedJunctions.add(newEdge.from);
        }
        if (newEdge.to.elementID == oldEdge.from.elementID || newEdge.to.elementID == oldEdge.to.elementID) {
            sharedJunctions.add(newEdge.to);
        }
        // Check the bits for collision.
        ArrayList<BlockPos> toBeRemoved = new ArrayList<>();
        for (GeoFeatureBit newBit : newEdge.getBits()) {
            for (GeoFeatureBit oldBit : oldEdge.getBits()) {
                // If the bits collide and their position is not close to one of the shared junctions, return true.
                if (newBit.blockPos.equals(oldBit.blockPos)) {
                    if (posIsInSameHeightRadii(newBit.blockPos, sharedJunctions)) {
                        toBeRemoved.add(newBit.blockPos);
                    } else {
                        return true;
                    }
                }
            }
        }
        // No collision; remove the overlapping bits and their sidewalk positions.
        newEdge.removeBits(toBeRemoved);
        return false;
    }
    private static boolean posIsInSameHeightRadii(BlockPos pos, ArrayList<RoadJunction> junctions) {
        for (RoadJunction junction : junctions) {
            if (posIsInSameHeightRadius(pos, junction)) {
                return true;
            }
        }
        return false;
    }
    private static boolean posIsInSameHeightRadius(BlockPos pos, RoadJunction junction) {
        return Math.pow(pos.getX()-junction.pos.getX(), 2) + Math.pow(pos.getZ()-junction.pos.getZ(), 2) <= junction.sameHeightRadius*junction.sameHeightRadius;
    }

    /**
     * Determines whether the access path collides with the road dot's edge. Bits of the access path that are close to
     * the road dot get removed unless they are overriding arch or sidewalk positions.
     * @param accessPath The access path that is being planned.
     * @param dot The road dot that the access path is trying to connect to.
     * @return True if the path collides with the edge.
     */
    static boolean accessPathCollidesWithEdge(RoadEdge accessPath, RoadDot dot) {
        RoadEdge edge = dot.edge;
        // Check bounds.
        if (!accessPath.boundsCollideWith(edge)) {
            return false;
        }
        // Check the bits for collision.
        int harmfulOverlappingBits = 0;
        boolean accessPathBitIsAir;
        boolean edgeBitIsAir;
        ArrayList<BlockPos> toBeRemoved = new ArrayList<>();
        double connectionPointRadiusSquared;
        for (GeoFeatureBit accessPathBit : accessPath.getBits()) {
            for (GeoFeatureBit edgeBit : edge.getBits()) {
                if (accessPathBit.blockPos.equals(edgeBit.blockPos)
                        && accessPathBit.blockState!=null && edgeBit.blockState!=null) {
                    // If the position is not close to the road dot, a collision is detected.
                    connectionPointRadiusSquared = Math.pow(2.0 + edge.radius + accessPath.radius, 2);
                    if (dot.pos.getSquaredDistance(edgeBit.blockPos) > connectionPointRadiusSquared) {
                        return true;
                    }
                    // Two bits close to the road to overlap. Remove the bit from the access path unless it should
                    // override arch/sidewalk positions of the edge.
                    if (!edge.archPositions.contains(edgeBit.blockPos)
                            && (!edge.sidewalkPositions.contains(edgeBit.blockPos)
                                || accessPath.archPositions.contains(edgeBit.blockPos))
                    ) {
                        toBeRemoved.add(edgeBit.blockPos);
                        // If the overlapping bits are air and non-air, count it as a harmful collision. If the number
                        // of those collisions is too high, return true.
                        accessPathBitIsAir = accessPathBit.blockState.isOf(Blocks.AIR)
                                || accessPath.sidewalkPositions.contains(accessPathBit.blockPos)
                                || accessPath.archPositions.contains(accessPathBit.blockPos);
                        edgeBitIsAir = edgeBit.blockState.isOf(Blocks.AIR)
                                || edge.sidewalkPositions.contains(edgeBit.blockPos)
                                || edge.archPositions.contains(edgeBit.blockPos);
                        if (accessPathBitIsAir != edgeBitIsAir) {
                            harmfulOverlappingBits++;
                            if (harmfulOverlappingBits > 3) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        // No collision; remove the overlapping bits.
        accessPath.removeBits(toBeRemoved);
        return false;
    }

    /**
     * Test for collisions between an access path and its structure. Bits in the access point's connection volume get
     * removed unless they are overriding arch or sidewalk positions.
     * @param structure The structure that the access path connects to.
     * @param accessPath The access path.
     * @return True if the access path collides with the structure.
     */
    static boolean accessPathCollidesWithItsStructure(Structure structure, StructureAccessPoint accessPoint,
                                                      RoadEdge accessPath) {
        // Check bounds.
        if (!structure.boundsCollideWith(accessPath)) {
            return false;
        }
        // Check the bits for collision.
        ArrayList<BlockPos> toBeRemoved = new ArrayList<>();
        for (GeoFeatureBit structureBit : structure.getBits()) {
            for (GeoFeatureBit accessPathBit : accessPath.getBits()) {
                if (structureBit.blockPos.equals(accessPathBit.blockPos)
                        && structureBit.blockState!=null && accessPathBit.blockState!=null) {
                    // If the position is not part of the connection volume, a collision is detected.
                    if (
                            structureBit.blockPos.getX() < Math.min(accessPoint.connectionVolume.get(0).getX(), accessPoint.connectionVolume.get(1).getX())
                            || structureBit.blockPos.getX() > Math.max(accessPoint.connectionVolume.get(0).getX(), accessPoint.connectionVolume.get(1).getX())
                            || structureBit.blockPos.getY() < Math.min(accessPoint.connectionVolume.get(0).getY(), accessPoint.connectionVolume.get(1).getY())
                            || structureBit.blockPos.getY() > Math.max(accessPoint.connectionVolume.get(0).getY(), accessPoint.connectionVolume.get(1).getY())
                            || structureBit.blockPos.getZ() < Math.min(accessPoint.connectionVolume.get(0).getZ(), accessPoint.connectionVolume.get(1).getZ())
                            || structureBit.blockPos.getZ() > Math.max(accessPoint.connectionVolume.get(0).getZ(), accessPoint.connectionVolume.get(1).getZ())
                    ) {
                        return true;
                    }
                    // Position is part of the connection volume. Remove the bit from the access path unless it should
                    // override arch/sidewalk positions of the access point.
                    if (!accessPoint.archPositions.contains(structureBit.blockPos)
                            && (!accessPoint.sidewalkPositions.contains(structureBit.blockPos)
                                || accessPath.archPositions.contains(structureBit.blockPos))
                    ) {
                        toBeRemoved.add(structureBit.blockPos);
                    }
                }
            }
        }
        // No collision; remove the overlapping bits.
        accessPath.removeBits(toBeRemoved);
        return false;
    }
}
