/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class Phase extends Module {
    public Phase() {
        super(Categories.Movement, "phase", "Allows you to phase through blocks.");
    }

    @EventHandler
    private void onMovement(PlayerMoveEvent e) {
        if (mc.player == null) return;

        if (mc.player.horizontalCollision && mc.options.forwardKey.isPressed()) {
            double rotation = Math.toRadians(mc.player.lastRenderYaw);
            double x = Math.sin(rotation);
            double z = Math.cos(rotation);

            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX() + x, mc.player.getY(), mc.player.getZ() + z, mc.player.isOnGround(), mc.player.horizontalCollision));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 11, mc.player.getZ(), mc.player.isOnGround(), mc.player.horizontalCollision));
            mc.player.setPos(mc.player.getX() - x, mc.player.getY(), mc.player.getZ() + z);
        }
    }
}
