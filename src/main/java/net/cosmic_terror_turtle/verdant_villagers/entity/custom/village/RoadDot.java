package net.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import net.cosmic_terror_turtle.verdant_villagers.util.NbtUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class RoadDot {

    public RoadEdge edge;
    public BlockPos pos;

    public RoadDot(RoadEdge edge, BlockPos pos) {
        this.edge = edge;
        this.pos = pos;
    }

    /**
     * Creates a new RoadDot from an NbtCompound.
     * @param nbt The compound representing a RoadDot.
     * @param edge The road edge this dot belongs to.
     */
    public RoadDot(@NotNull NbtCompound nbt, RoadEdge edge) {
        this.edge = edge;
        pos = NbtUtils.blockPosFromNbt(nbt.getCompound("pos"));
    }
    /**
     * Saves this RoadDot to an NbtCompound.
     * @return The compound representing this RoadDot.
     */
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("pos", NbtUtils.blockPosToNbt(pos));
        return nbt;
    }
}
