package io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom;

import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.ClientVillage;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.ServerVillage;
import io.github.cosmic_terror_turtle.ctt_verdant_villagers.entity.custom.village.Village;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityChangeListener;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

public class VillageHeartEntity extends PathAwareEntity implements GeoEntity {

    public static final double MAX_ANCHOR_CALL_DISTANCE = 100.0;
    public static final double ANCHOR_HOVER_HEIGHT = 5;
    public static final double MIN_DISTANCE_BETWEEN_HEARTS = 800.0;

    private final AnimatableInstanceCache cache;

    private boolean initialized;
    private Village village;

    public VillageHeartEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        moveControl = new FlightMoveControl(this, 20, true);
        cache = new SingletonAnimatableInstanceCache(this);

        // Pre-initialization
        initialized = false;
        village = null;
    }

    public static DefaultAttributeContainer.Builder createVillageHeartAttributes() {
        return createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, MAX_ANCHOR_CALL_DISTANCE);
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
        return birdNavigation;
    }

    public void setTargetPosition(Vec3d v, double speedModifier) {
        if (getNavigation().isIdle()) {
            getNavigation().startMovingTo(v.x, v.y, v.z, speedModifier);
        }
    }

    @Override
    public void setChangeListener(EntityChangeListener entityChangeListener) {
        // This method gets called in ServerEntityManager#unload(EntityLike) whenever an entity gets unloaded
        // together with its chunk.
        super.setChangeListener(entityChangeListener);
        if (getRemovalReason() == RemovalReason.UNLOADED_TO_CHUNK) {
            removeVillageOnServer();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        removeVillageOnServer();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        village = null;
    }

    private void removeVillageOnServer() {
        if (village instanceof ServerVillage serverVillage) {
            serverVillage.remove();
        }
        village = null;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        // The village heart can only be damaged by creative mode players.
        if (source.isSourceCreativePlayer()) {
            return super.damage(source, amount);
        } else {
            return false;
        }
    }

    @Override
    public boolean canUsePortals() {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public boolean cannotDespawn() {
        return true;
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

        // Only perform a village tick if no other village hearts are close.
        if (village!=null && world.getEntitiesByClass(
                VillageHeartEntity.class,
                new Box(getBlockPos()).expand(MIN_DISTANCE_BETWEEN_HEARTS),
                entity -> true
        ).size() <= 1) {
            village.tick();
        }
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }
        if (village instanceof ServerVillage serverVillage) {
            // Adjust villager count if allowed.
            if (!ServerVillage.countVillagers) {
                if (player.isSneaking()) {
                    serverVillage.changeVillagerCount(-ServerVillage.VILLAGER_COUNT_DELTA);
                } else {
                    serverVillage.changeVillagerCount(ServerVillage.VILLAGER_COUNT_DELTA);
                }
            }
        } else if (village instanceof ClientVillage clientVillage) {
            // Open screen for player.
        } else {
            return ActionResult.PASS;
        }
        return ActionResult.success(world.isClient());
    }


    // Animation methods

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> state) {
        state.getController().setAnimation(RawAnimation.begin().then("animation.village_heart.hover", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
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
            nbt.putInt("villagerCount", village.getVillagerCount());
        }

        return nbt;
    }

    /**
     * Sets the synced data of the village.
     * @param nbt The NbtCompound used for syncing.
     */
    private void setSyncNbt(NbtCompound nbt) {
        if (village!=null && nbt.contains("villagerCount")) {
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
