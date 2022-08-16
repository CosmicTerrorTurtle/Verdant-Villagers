package net.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import net.cosmic_terror_turtle.verdant_villagers.VerdantVillagers;
import net.cosmic_terror_turtle.verdant_villagers.data.village.BlockStateParsing;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;

public class RoadType {

    private static final BlockState air = Blocks.AIR.getDefaultState();
    private static final BlockState dirt = Blocks.DIRT.getDefaultState();
    private static final BlockState dirtPath = Blocks.DIRT_PATH.getDefaultState();
    private static final BlockState torch = Blocks.TORCH.getDefaultState();

    private static double getJunctionRadius(double edgeRadius) {
        return 1 + 2.5*edgeRadius;
    }
    private static double getJunctionSameHeightRadius(double edgeRadius) {
        return 1 + 4.0*edgeRadius;
    }

    public static RoadType getSmallPath() {
        return new RoadType(
                2.0,
                new double[]{2.0},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 4)},
                1000000.0,
                null,
                new double[]{getJunctionRadius(2.0)},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 6)}
        );
    }
    public static RoadType getMediumPath(ServerVillage village) {
        BlockState log = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "wood"), 0).getElement("log_axis_y"));
        BlockState fence = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "wood"), 0).getElement("fence_waterlogged_false_north_false_south_false_west_false_east_false"));
        return new RoadType(
                2.5,
                new double[]{2.5},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 5)},
                6.0,
                new VerticalBlockColumn(new BlockState[]{dirt, log, log, log, fence, torch}, 1, air, 1),
                new double[]{
                        0.0,
                        getJunctionRadius(2.5)
                },
                new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{dirt, log, log, log, fence, torch}, 1, air, 4),
                        new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 7)
                }
        );
    }
    public static RoadType getBigPath(ServerVillage village) {
        BlockState log = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "wood"), 0).getElement("log_axis_y"));
        BlockState fence = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "wood"), 0).getElement("fence_waterlogged_false_north_false_south_false_west_false_east_false"));
        return new RoadType(
                3.0,
                new double[]{3.0},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 6)},
                8.0,
                new VerticalBlockColumn(new BlockState[]{dirt, log, log, log, fence, torch}, 1, air, 2),
                new double[]{
                        0.0,
                        getJunctionRadius(3.0)
                },
                new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{dirt, log, log, log, fence, torch}, 1, air, 6),
                        new VerticalBlockColumn(new BlockState[]{dirt, dirtPath}, 1, air, 8)
                }
        );
    }
    public static RoadType getSmallStreet(ServerVillage village) {
        BlockState full0 = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "stone"), 0).getElement("full"));
        BlockState full1 = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "stone"), 1).getElement("full"));
        BlockState wall = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "stone"), 0).getElement("wall_waterlogged_false_up_true_north_none_south_none_west_none_east_none"));
        return new RoadType(
                3.0,
                new double[]{3.0},
                new VerticalBlockColumn[]{new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 6)},
                8.0,
                new VerticalBlockColumn(new BlockState[]{full0, full1, full1, full1, wall, torch}, 1, air, 2),
                new double[]{
                        0.0,
                        getJunctionRadius(3.0)
                },
                new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{full0, full1, full1, full1, wall, torch}, 1, air, 6),
                        new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 8)
                }
        );
    }
    public static RoadType getMediumStreet(ServerVillage village) {
        BlockState full0 = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "stone"), 0).getElement("full"));
        BlockState full1 = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "stone"), 1).getElement("full"));
        BlockState wall = BlockStateParsing.parsePlainBlockState(village.getBlockPaletteOf(new Identifier(VerdantVillagers.MOD_ID, "stone"), 0).getElement("wall_waterlogged_false_up_true_north_none_south_none_west_none_east_none"));
        return new RoadType(
                3.5,
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
                new double[]{
                        0.0,
                        getJunctionRadius(3.5)
                },
                new VerticalBlockColumn[]{
                        new VerticalBlockColumn(new BlockState[]{full0, full1, full1, full1, wall, torch}, 1, air, 8),
                        new VerticalBlockColumn(new BlockState[]{full0, full0}, 1, air, 9)
                }
        );
    }

    public double edgeRadius;
    public double[] edgeBlockColumnRadii;
    public VerticalBlockColumn[] edgeTemplateBlockColumns;
    public double edgeMiddleColumnSpace;
    public VerticalBlockColumn edgeTemplateMiddleColumn;

    public double junctionRadius;
    public double junctionSameHeightRadius;
    public double[] junctionBlockColumnRadii;
    public VerticalBlockColumn[] junctionTemplateBlockColumns;

    public RoadType(double edgeRadius,
                    double[] edgeBlockColumnRadii, VerticalBlockColumn[] edgeTemplateBlockColumns,
                    double edgeMiddleColumnSpace, VerticalBlockColumn edgeTemplateMiddleColumn,
                    double[] junctionBlockColumnRadii, VerticalBlockColumn[] junctionTemplateBlockColumns) {
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
