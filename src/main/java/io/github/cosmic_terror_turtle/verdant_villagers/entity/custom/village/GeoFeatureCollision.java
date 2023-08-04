package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadEdge;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road.RoadJunction;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class GeoFeatureCollision {

    /**
     * Determines if the features overlap.
     * @param feature1 First input.
     * @param feature2 Second input.
     * @param useMegaBlocks Whether the positions of the mega blocks or of the feature bits will be analyzed.
     * @return True if they overlap.
     */
    static boolean featuresOverlap(GeoFeature feature1, GeoFeature feature2, boolean useMegaBlocks) {
        // Check bounds.
        if (!feature1.boundsCollideWith(feature2)) {
            return false;
        }

        // Check the mega blocks or bits for collision.
        if (useMegaBlocks) {
            for (BlockPos feature1BlockPosition : feature1.getTouchedMegaBlocks()) {
                for (BlockPos feature2BlockPosition : feature2.getTouchedMegaBlocks()) {
                    if (feature1BlockPosition.equals(feature2BlockPosition)) {
                        return true;
                    }
                }
            }
        } else {
            for (GeoFeatureBit feature1Bit : feature1.getBits()) {
                for (GeoFeatureBit feature2Bit : feature2.getBits()) {
                    if (feature1Bit.blockPos.equals(feature2Bit.blockPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean featuresOverlapIgnoreMatchingBlockStates(GeoFeature feature1, GeoFeature feature2) {
        // Check bounds.
        if (!feature1.boundsCollideWith(feature2)) {
            return false;
        }

        // Check the bits for collision.
        for (GeoFeatureBit feature1Bit : feature1.getBits()) {
            for (GeoFeatureBit feature2Bit : feature2.getBits()) {
                if (feature1Bit.blockPos.equals(feature2Bit.blockPos)
                        && feature1Bit.blockState!=null && feature2Bit.blockState!=null
                        && !feature1Bit.blockState.equals(feature2Bit.blockState)) {
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
        ArrayList<GeoFeatureBit> toBeRemoved = new ArrayList<>();
        for (GeoFeatureBit newBit : newEdge.getBits()) {
            for (GeoFeatureBit oldBit : oldEdge.getBits()) {
                // If the bits collide and their position is not close to one of the shared junctions, return true.
                if (newBit.blockPos.equals(oldBit.blockPos)) {
                    if (posIsInSameHeightRadii(newBit.blockPos, sharedJunctions)) {
                        toBeRemoved.add(newBit);
                    } else {
                        return true;
                    }
                }
            }
        }
        // No collision; remove the overlapping bits and their sidewalk positions.
        for (GeoFeatureBit bit : toBeRemoved) {
            newEdge.getBits().remove(bit);
            newEdge.sidewalkPositions.remove(bit.blockPos);
            newEdge.archPositions.remove(bit.blockPos);
            newEdge.pillarStartBits.removeIf(pillarBit -> pillarBit.blockPos.equals(bit.blockPos));
        }
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
     * Determines whether the access path collides with the edge. When two bits overlap that are either both air or both
     * not air, this does not count as a collision. Instead, the bit of the access path gets removed from it.
     * @param accessPath The access path that is being planned.
     * @param edge The edge that the access path is trying to connect to.
     * @return True if the path collides with the edge.
     */
    static boolean accessPathCollidesWithEdge(RoadEdge accessPath, RoadEdge edge) {
        // Check bounds.
        if (!accessPath.boundsCollideWith(edge)) {
            return false;
        }

        // Check the bits for collision.
        ArrayList<GeoFeatureBit> toBeRemoved = new ArrayList<>();
        for (GeoFeatureBit accessPathBit : accessPath.getBits()) {
            for (GeoFeatureBit edgeBit : edge.getBits()) {
                if (accessPathBit.blockPos.equals(edgeBit.blockPos) && accessPathBit.blockState!=null && edgeBit.blockState!=null) {
                    // If the position is part of edge's sidewalk or arch, continue (this way, it will be overwritten by the access path).
                    if (edge.sidewalkPositions.contains(edgeBit.blockPos) || edge.archPositions.contains(edgeBit.blockPos)) {
                        continue;
                    }
                    // Are both bits air or both bits non-air?
                    if ((accessPathBit.blockState.isOf(Blocks.AIR) && edgeBit.blockState.isOf(Blocks.AIR))
                            || (!accessPathBit.blockState.isOf(Blocks.AIR) && !edgeBit.blockState.isOf(Blocks.AIR))) {
                        toBeRemoved.add(accessPathBit);
                    } else {
                        return true;
                    }
                }
            }
        }
        // No collision; remove the overlapping bits.
        for (GeoFeatureBit bit : toBeRemoved) {
            accessPath.getBits().remove(bit);
        }
        return false;
    }
}
