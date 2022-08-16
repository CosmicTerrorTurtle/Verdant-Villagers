package net.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import net.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;

public class Village {

    protected final VillageHeartEntity villageHeart;
    protected String name = "";
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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void tick() {
    }
}
