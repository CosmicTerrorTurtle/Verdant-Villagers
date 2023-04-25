package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.road;

import io.github.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import io.github.cosmic_terror_turtle.verdant_villagers.data.village.BlockStateParsing;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.VerticalBlockColumn;
import io.github.cosmic_terror_turtle.verdant_villagers.util.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;

public class RoadType {

    private static final BlockState air = Blocks.AIR.getDefaultState();
    private static final BlockState dirt = Blocks.DIRT.getDefaultState();
    private static final BlockState dirtPath = Blocks.DIRT_PATH.getDefaultState();
    private static final BlockState water = Blocks.WATER.getDefaultState();
    private static final BlockState torch = Blocks.TORCH.getDefaultState();

    private static double getJunctionRadius(double edgeRadius) {
        return 1 + 2.5*edgeRadius;
    }
    private static double getJunctionSameHeightRadius(double edgeRadius) {
        return 1 + 4.0*edgeRadius;
    }

    public static RoadType getSmallPath() {
        return new RoadType(
                1.0, 2.0,
                new double[]{2.0},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 4)},
                1000000.0,
                null,
                new double[]{getJunctionRadius(2.0)},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 6)}
        );
    }
    public static RoadType getMediumPath(ServerVillage village) {
        BlockState log = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "wood", Blocks.OAK_LOG.getDefaultState(), 0);
        BlockState fence = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "wood", Blocks.OAK_FENCE.getDefaultState(), 0);
        return new RoadType(
                1.1, 2.5,
                new double[]{2.5},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 5)},
                6.0,
                new VerticalBlockColumn(new BlockState[]{dirt, log, log, log, fence, torch}, 1, air, 1),
                new double[]{
                        0.0,
                        getJunctionRadius(2.5)
                },
                new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{dirt, log, log, log, fence, torch}, 1, air, 3),
                        new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 7)
                }
        );
    }
    public static RoadType getBigPath(ServerVillage village) {
        BlockState log = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "wood", Blocks.OAK_LOG.getDefaultState(), 0);
        BlockState fence = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "wood", Blocks.OAK_FENCE.getDefaultState(), 0);
        return new RoadType(
                1.2, 3.0,
                new double[]{3.0},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 6)},
                8.0,
                new VerticalBlockColumn(new BlockState[]{dirt, log, log, log, fence, torch}, 1, air, 2),
                new double[]{
                        0.0,
                        getJunctionRadius(3.0)
                },
                new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{dirt, log, log, log, fence, torch}, 1, air, 4),
                        new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 8)
                }
        );
    }
    public static RoadType getSmallStreet(ServerVillage village) {
        BlockState full0 = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "stone", Blocks.STONE_BRICKS.getDefaultState(), 0);
        BlockState full1 = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "stone", Blocks.STONE_BRICKS.getDefaultState(), 1);
        BlockState wall = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "stone", Blocks.STONE_BRICK_WALL.getDefaultState(), 0);
        return new RoadType(
                1.3, 3.0,
                new double[]{3.0},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 6)},
                8.0,
                new VerticalBlockColumn(new BlockState[]{full0, full1, full1, full1, wall, torch}, 1, air, 2),
                new double[]{
                        0.0,
                        getJunctionRadius(3.0)
                },
                new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{full0, full1, full1, full1, wall, torch}, 1, air, 4),
                        new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 8)
                }
        );
    }
    public static RoadType getMediumStreet(ServerVillage village) {
        BlockState log = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "wood", Blocks.OAK_LOG.getDefaultState(), 0);
        BlockState leaves = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "wood", Blocks.OAK_LEAVES.getDefaultState().with(Properties.PERSISTENT, true), 0);
        BlockState full0 = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "stone", Blocks.STONE_BRICKS.getDefaultState(), 0);
        BlockState full1 = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "stone", Blocks.STONE_BRICKS.getDefaultState(), 1);
        BlockState half1 = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "stone", Blocks.STONE_BRICK_SLAB.getDefaultState(), 1);
        BlockState wall = BlockStateParsing.getBlockStateFrom(
                village, VerdantVillagers.MOD_ID, "stone", Blocks.STONE_BRICK_WALL.getDefaultState(), 0);
        double[] junctionBlockColumnRadii;
        VerticalBlockColumn[] junctionTemplateBlockColumns;
        int i = MathUtils.nextInt(0, 3);
        switch (i) {
            default:
            case 0:
                // Lamp post
                junctionBlockColumnRadii = new double[]{
                        0.0,
                        getJunctionRadius(3.5)
                };
                junctionTemplateBlockColumns = new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{full0, full1, full1, full1, wall, torch}, 1, air, 5),
                        new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 9)
                };
                break;
            case 1:
                // Water well
                junctionBlockColumnRadii = new double[]{
                        0.0,
                        1.0,
                        2.0,
                        getJunctionRadius(3.5)
                };
                junctionTemplateBlockColumns = new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{full0, full0, water}, 1, air, 8),
                        new VerticalBlockColumn(new BlockState[]{full0, full0, full1}, 1, air, 8),
                        new VerticalBlockColumn(new BlockState[]{full0, full0, half1}, 1, air, 8),
                        new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 9)
                };
                break;
            case 2:
                // Bush
                junctionBlockColumnRadii = new double[]{
                        0.0,
                        getJunctionRadius(3.5)
                };
                junctionTemplateBlockColumns = new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{full0, dirt, leaves, leaves, leaves}, 1, air, 6),
                        new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 9)
                };
                break;
            case 3:
                // Small tree
                junctionBlockColumnRadii = new double[]{
                        0.0,
                        1.0,
                        getJunctionRadius(3.5)
                };
                junctionTemplateBlockColumns = new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{full0, dirt, log, log, log, leaves, leaves, leaves}, 1, air, 3),
                        new VerticalBlockColumn(new BlockState[]{full0, full0, air, air, leaves, leaves}, 1, air, 5),
                        new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 9)
                };
        }
        return new RoadType(
                1.5, 3.5,
                new double[]{
                        2.5,
                        3.5
                },
                new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 7),
                        new VerticalBlockColumn(new BlockState[]{full0, full1}, 1, air, 7)
                },
                8.0,
                new VerticalBlockColumn(new BlockState[]{full0, full1, full1, full1, wall, torch}, 1, air, 3),
                junctionBlockColumnRadii,
                junctionTemplateBlockColumns
        );
    }


    public double edgeMinMaxLengthMultiplier;

    public double edgeRadius;
    public double[] edgeBlockColumnRadii;
    public VerticalBlockColumn[] edgeTemplateBlockColumns;
    public double edgeMiddleColumnSpace;
    public VerticalBlockColumn edgeTemplateMiddleColumn;

    public double junctionRadius;
    public double junctionSameHeightRadius;
    public double[] junctionBlockColumnRadii;
    public VerticalBlockColumn[] junctionTemplateBlockColumns;

    public RoadType(double edgeMinMaxLengthMultiplier, double edgeRadius,
                    double[] edgeBlockColumnRadii, VerticalBlockColumn[] edgeTemplateBlockColumns,
                    double edgeMiddleColumnSpace, VerticalBlockColumn edgeTemplateMiddleColumn,
                    double[] junctionBlockColumnRadii, VerticalBlockColumn[] junctionTemplateBlockColumns) {
        this.edgeMinMaxLengthMultiplier = edgeMinMaxLengthMultiplier;
        this.edgeRadius = edgeRadius;
        this.edgeBlockColumnRadii = edgeBlockColumnRadii;
        this.edgeTemplateBlockColumns = edgeTemplateBlockColumns;
        this.edgeMiddleColumnSpace = edgeMiddleColumnSpace;
        this.edgeTemplateMiddleColumn = edgeTemplateMiddleColumn;
        junctionRadius = getJunctionRadius(edgeRadius);
        junctionSameHeightRadius = getJunctionSameHeightRadius(edgeRadius);
        this.junctionBlockColumnRadii = junctionBlockColumnRadii;
        this.junctionTemplateBlockColumns = junctionTemplateBlockColumns;
    }
}
