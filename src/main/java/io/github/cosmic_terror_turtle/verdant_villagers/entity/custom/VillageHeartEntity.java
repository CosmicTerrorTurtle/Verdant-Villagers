package io.github.cosmic_terror_turtle.verdant_villagers.entity.custom;

import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ClientVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.verdant_villagers.entity.custom.village.Village;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class VillageHeartEntity extends PathAwareEntity implements IAnimatable {

    public static final double ANCHOR_DISTANCE = 100.0;
    public static final double ANCHOR_HOVER_HEIGHT = 5;

    private final AnimationFactory factory;

    private boolean initialized;
    private Village village;

    public VillageHeartEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        moveControl = new FlightMoveControl(this, 20, true);
        factory = new AnimationFactory(this);

        // Pre-initialization
        initialized = false;
        village = null;
    }

    public static DefaultAttributeContainer.Builder createVillageHeartAttributes() {
        return createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, ANCHOR_DISTANCE);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world) {
            @Override
            public boolean isValidPosition(BlockPos pos) {
                return true;
            }
            @Override
            protected boolean isAtValidPosition() {
                return true;
            }
            @Override
            protected boolean canPathDirectlyThrough(Vec3d origin, Vec3d target) {
                return true;
            }
        };
        birdNavigation.setCanPathThroughDoors(true);
        birdNavigation.setCanEnterOpenDoors(true);
        birdNavigation.setCanSwim(true);
        return birdNavigation;
    }

    public void setTargetPosition(Vec3d v, double speedModifier) {
        if (getNavigation().isIdle()) {
            getNavigation().startMovingTo(v.x, v.y, v.z, speedModifier);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source != DamageSource.OUT_OF_WORLD) {
            // activate this to make the village heart invulnerable
            //return false;
        }
        return super.damage(source, amount);
    }

    @Override
    public boolean canUsePortals() {
        return false;
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;
    }

    private void customInitialize() {
        if (world.isClient()) {
            village = new ClientVillage(this);
        } else {
            village = new ServerVillage(this);
            markForUpdate();
        }

        initialized = true;
    }

    @Override
    public void tick() {
        if (!initialized) {
            customInitialize();
        }

        noClip = true;
        setNoGravity(true);
        super.tick();

        if (village!=null) {
            village.tick();
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (village instanceof ServerVillage && hand == Hand.MAIN_HAND) {
            ((ServerVillage) village).increaseVillagerCount();
            return ActionResult.success(world.isClient());
        }

        return super.interactMob(player, hand);
    }


    // Animation methods

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.village_heart.hover", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }


    // Tracked data

    private static final TrackedData<NbtCompound> VILLAGE_NBT_SYNC = DataTracker.registerData(VillageHeartEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(VILLAGE_NBT_SYNC, new NbtCompound());
    }

    /**
     * Gets the tracked data from the village and sets it on the tracker.
     */
    public void markForUpdate() {
        dataTracker.set(VILLAGE_NBT_SYNC, getSyncNbt());
    }

    /**
     * Sets the tracked data on the village.
     */
    public void syncData() {
        setSyncNbt(dataTracker.get(VILLAGE_NBT_SYNC));
    }

    /**
     * Creates the NbtCompound used for syncing data between client and server from the village.
     * @return The NbtCompound.
     */
    private NbtCompound getSyncNbt() {
        NbtCompound nbt = new NbtCompound();

        if (village!=null) {
            nbt.putString("name", village.getName());
            nbt.putInt("villagerCount", village.getVillagerCount());
        }

        return nbt;
    }

    /**
     * Sets the synced data of the village.
     * @param nbt The NbtCompound used for syncing.
     */
    private void setSyncNbt(NbtCompound nbt) {
        if (village!=null && nbt.contains("name")) {
            village.setName(nbt.getString("name"));
            village.setVillagerCount(nbt.getInt("villagerCount"));
        }
    }


    // Saving and loading data

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        if (village!=null) {
            nbt.put("village", ((ServerVillage) village).toNbt());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (world.isClient()) {
            village = new ClientVillage(this);
        } else {
            village = new ServerVillage(this, nbt.getCompound("village"));
            markForUpdate();
        }

        initialized = true;
    }
}
