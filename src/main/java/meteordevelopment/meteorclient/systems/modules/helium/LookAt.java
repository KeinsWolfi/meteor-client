/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LookAt extends Module {

    public final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Entities to look at.")
            .defaultValue(EntityType.PLAYER)
            .build()
    );


    public LookAt() {
        super(Categories.Helium, "LookAt", "Makes you look at the nearest player.");
    }

    private final List<Entity> targets = new ArrayList<>();

    @Override
    public void onDeactivate() {
        targets.clear();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event){
        targets.clear();
        TargetUtils.getList(targets, this::isGood, SortPriority.LowestDistance, 100000);

        if(targets.isEmpty()) return;

        Entity target = targets.get(0);

        double yaw = Rotations.getYaw(target);
        double pitch = Rotations.getPitch(target);

        Rotations.rotate(yaw, pitch, 0, null);
    }

    private boolean isGood(Entity entity){
        if(!entities.get().contains(entity.getType())) return false;
        return entity instanceof LivingEntity && entity != mc.player && entity.isAlive();
    }


}
