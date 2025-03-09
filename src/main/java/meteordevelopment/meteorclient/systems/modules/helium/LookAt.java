/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3d;

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

    public final Setting<SortPriority> sort = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("Sort")
        .description("How to sort the entities.")
        .defaultValue(SortPriority.LowestDistance)
        .build()
    );


    public LookAt() {
        super(Categories.Helium, "LookAt", "Makes you look at the nearest player.");
    }

    private final Color lineColor = new Color();
    private final Color sideColor = new Color();
    private final Color baseColor = new Color();

    private final Vector3d pos1 = new Vector3d();
    private final Vector3d pos2 = new Vector3d();
    private final Vector3d pos = new Vector3d();

    Entity target;

    private final List<Entity> targets = new ArrayList<>();

    @Override
    public void onDeactivate() {
        targets.clear();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event){
        targets.clear();
        TargetUtils.getList(targets, this::isGood, sort.get(), 100000);

        if(targets.isEmpty()) return;

        target = targets.get(0);

        double yaw = Rotations.getYaw(target);
        double pitch = Rotations.getPitch(target);

        Rotations.rotate(yaw, pitch, 0, null);

    }

    @EventHandler
    public void onRender(Render3DEvent event){
        if(targets.isEmpty()) return;

        drawBoundingBox(event, target);

    }

    private boolean isGood(Entity entity){
        if(!entities.get().contains(entity.getType())) return false;
        return entity instanceof LivingEntity && entity != mc.player && entity.isAlive();
    }

    private void drawBoundingBox(Render3DEvent event, Entity entity) {
        Color color = new Color(200, 200, 200, 255);
        if (color != null) {
            lineColor.set(color);
            sideColor.set(color).a((int) (sideColor.a * 0.3f));
        }

        //if (mode.get() == ESP.Mode.Box) {
        double x = MathHelper.lerp(event.tickDelta, entity.lastRenderX, entity.getX()) - entity.getX();
        double y = MathHelper.lerp(event.tickDelta, entity.lastRenderY, entity.getY()) - entity.getY();
        double z = MathHelper.lerp(event.tickDelta, entity.lastRenderZ, entity.getZ()) - entity.getZ();
        Box box = entity.getBoundingBox();
        event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, sideColor, lineColor, ShapeMode.Both, 0);
        /*} else {
            WireframeEntityRenderer.render(event, entity, 1, sideColor, lineColor, shapeMode.get());
        }*/
    }


}
