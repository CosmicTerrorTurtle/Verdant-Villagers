package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.VillageHeartEntity;

public abstract class Village {

    protected final VillageHeartEntity villageHeart;
    protected int villagerCount = 0;

    public Village(VillageHeartEntity villageHeart) {
        this.villageHeart = villageHeart;
    }

    public int getVillagerCount() {
        return villagerCount;
    }
    public void setVillagerCount(int count) {
        villagerCount = count;
    }

    public abstract void tick();
}
