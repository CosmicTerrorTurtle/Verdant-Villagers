package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;

public class Village {

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

    public void tick() {

    }
}
