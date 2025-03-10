/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedList;
import java.util.List;

public class ChestAura extends Module {
    LinkedList<Entity> chests = new LinkedList<>();
    Long lastAttack = 0L;

    public ChestAura() {
        super(Categories.Helium, "chest-aura", "Automatically opens chests around you.");
    }

    @EventHandler
    public void onTick(TickEvent.Pre e) {
        if (mc.world == null) return;
        Iterable<BlockEntity> entities = Utils.blockEntities();

        for (BlockEntity entity : entities) {
            if (entity instanceof TrappedChestBlockEntity chest) {
                boolean valid = false;
                boolean slimeFound = false;
                boolean textEntityFound = false;
                Text textEntityText = null;
                SlimeEntity slimeEntity = null;

                List<Entity> closestEntities = EntityUtils.getEntitiesInRange(chest.getPos().toCenterPos(), 2.0);
                if (closestEntities == null) continue;

                for (Entity closestEntity : closestEntities) {
                    if (closestEntity instanceof SlimeEntity slime) {
                        slimeEntity = slime;
                        if (chests != null) {
                            slimeFound = !chests.contains(slime);
                        } else {
                            slimeFound = true;
                        }
                    }

                    if (closestEntity instanceof DisplayEntity.TextDisplayEntity textEntity) {
                        if (textEntity.getData() == null) continue;
                        if (isValidTextEntity(textEntity.getData().text())) {
                            textEntityText = textEntity.getData().text();
                            textEntityFound = true;
                        }
                    }

                    if (slimeFound && textEntityFound) {
                        if (mc.currentScreen != null) return;
                        if (chests == null) chests = new LinkedList<>();
                        valid = true;
                        chests.add(slimeEntity);
                        ChatUtils.info("Chest found at: " + chest.getPos().toShortString() + " with text: " + textEntityText.getString());
                    }
                }
            }
        }

        if (mc.currentScreen != null || chests == null || chests.isEmpty()) return;

        chests.sort((a, b) -> {
            double distanceA = a.squaredDistanceTo(mc.player);
            double distanceB = b.squaredDistanceTo(mc.player);
            return Double.compare(distanceA, distanceB);
        });

        Entity target = chests.getFirst();

        if (target == null) return;

        Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target, Target.Body));
        attack(target);
    }

    private boolean isValidTextEntity(Text text) {
        return text.getString().contains("Loot Chest");
    }

    private void attack(Entity target) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastAttack < 1000) return;
        Box hitbox = target.getBoundingBox();
        if (!PlayerUtils.isWithin(
            MathHelper.clamp(mc.player.getX(), hitbox.minX, hitbox.maxX),
            MathHelper.clamp(mc.player.getY(), hitbox.minY, hitbox.maxY),
            MathHelper.clamp(mc.player.getZ(), hitbox.minZ, hitbox.maxZ),
            6
        )) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        chests.remove(target);
        lastAttack = System.currentTimeMillis();
    }
}
