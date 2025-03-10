/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.entity.player.InteractEntityEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.text.Text;

import java.util.List;

public class ChestAura extends Module {
    List<TrappedChestBlockEntity> chests;

    public ChestAura() {
        super(Categories.Helium, "chest-aura", "Automatically opens chests around you.");
    }

    @EventHandler
    public void onTick(TickEvent.Post e) {
        if (mc.world == null) return;
        Iterable<BlockEntity> entities = Utils.blockEntities();

        for (BlockEntity entity : entities) {
            if (entity instanceof TrappedChestBlockEntity chest) {
                boolean valid = false;
                boolean slimeFound = false;
                boolean textEntityFound = false;
                Text textEntityText = null;

                List<Entity> closestEntities = EntityUtils.getEntitiesInRange(chest.getPos().toCenterPos(), 2.0);
                if (closestEntities == null) continue;

                for (Entity closestEntity : closestEntities) {
                    if (closestEntity instanceof SlimeEntity slime) {
                        System.out.println("SLIME FOUND!");
                        slimeFound = true;
                    }

                    if (closestEntity instanceof DisplayEntity.TextDisplayEntity textEntity) {
                        System.out.println("TEXT ENTITY FOUND: " + textEntity.getData().text().getString());
                        if (isValidTextEntity(textEntity.getData().text())) {
                            textEntityText = textEntity.getData().text();
                            textEntityFound = true;
                        }
                    }

                    if (slimeFound && textEntityFound) {
                        valid = true;
                        ChatUtils.info("Chest found at: " + chest.getPos().toShortString() + " with text: " + textEntityText.getString());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(InteractEntityEvent e) {
        System.out.println("Interact event with: " + e.entity.getType().toString() + " at: " + e.entity.getPos().toString());
    }

    private boolean isValidTextEntity(Text text) {
        if (text.getString().contains("Loot Chest")) {
            return true;
        }
        return false;
    }
}
