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

    static boolean featuresOverlapIgnoreMatchingBlocks(GeoFeature feature1, GeoFeature feature2) {
        // Check bounds.
        if (!feature1.boundsCollideWith(feature2)) {
            return false;
        }

        // Check the bits for collision.
        for (GeoFeatureBit feature1Bit : feature1.getBits()) {
            for (GeoFeatureBit feature2Bit : feature2.getBits()) {
                if (feature1Bit.blockPos.equals(feature2Bit.blockPos)
                        && feature1Bit.blockState!=null && feature2Bit.blockState!=null
                        && !feature1Bit.blockState.isOf(feature2Bit.blockState.getBlock())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines whether two edges overlap, but ignores block positions that are within the same height radius of
     * the junctions that the edges share.
     * @param edge1 The first edge.
     * @param edge2 The second Edge.
     * @return True if the edges overlap.
     */
    static boolean edgesOverlap(RoadEdge edge1, RoadEdge edge2) {
        // Check bounds.
        if (!edge1.boundsCollideWith(edge2)) {
            return false;
        }

        // Check the bits for collision.
        for (GeoFeatureBit bit1 : edge1.getBits()) {
            for (GeoFeatureBit bit2 : edge2.getBits()) {
                if (!posIsInSameHeightRadius(bit1.blockPos, edge1.from) && !posIsInSameHeightRadius(bit1.blockPos, edge1.to)
                    && bit1.blockPos.equals(bit2.blockPos)) {
                   return true;
                }
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
                if (accessPathBit.blockPos.equals(edgeBit.blockPos)) {
                    if (accessPathBit.blockState!=null && edgeBit.blockState!=null) {
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
        }
        // No collision; remove the overlapping bits.
        for (GeoFeatureBit bit : toBeRemoved) {
            accessPath.getBits().remove(bit);
        }
        return false;
    }
}
