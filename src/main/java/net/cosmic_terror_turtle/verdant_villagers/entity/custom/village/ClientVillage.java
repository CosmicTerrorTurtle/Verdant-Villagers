package net.cosmic_terror_turtle.verdant_villagers.entity.custom.village;

import net.cosmic_terror_turtle.verdant_villagers.entity.custom.VillageHeartEntity;

public class ClientVillage extends Village {

    public ClientVillage(VillageHeartEntity villageHeart) {
        super(villageHeart);
    }

    @Override
    public void tick() {
        villageHeart.syncData();
    }
}
