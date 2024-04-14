/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.player.Reach;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Replant extends Module {

    public SettingGroup sgGeneral = settings.getDefaultGroup();

    public Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Blocks to replant.")
        .defaultValue(Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.NETHER_WART, Blocks.BEETROOTS, Blocks.KELP, Blocks.KELP_PLANT)
        .filter(block -> block instanceof CropBlock)
        .build()
    );

    public Setting<Boolean> checkGrowth = sgGeneral.add(new BoolSetting.Builder()
        .name("check-growth")
        .description("Checks, if crop is fully grown before replanting.")
        .defaultValue(true)
        .build()
    );

    public Replant() {
        super(Categories.Helium, "Replant", "Automatically replants crops.");
    }

    @EventHandler
    private void onRightClick(InteractBlockEvent event) {
        BlockHitResult result = event.result;
        BlockPos blockPos = result.getBlockPos();
        Block block = mc.world.getBlockState(blockPos).getBlock();
        if(blocks.get().contains(block)) {
            if(block instanceof CropBlock && checkGrowth.get()){
                if(!((CropBlock)block).isMature(mc.world.getBlockState(blockPos))) return;
            }
            FindItemResult findSeeds = InvUtils.findInHotbar(getSeeds(block));
            if(findSeeds.found()) {
                InvUtils.swap(findSeeds.slot(), true);
            }
            mc.interactionManager.breakBlock(blockPos);
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockPos, Direction.SOUTH));
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.SOUTH));
            mc.interactionManager.interactBlock(mc.player, mc.player.getActiveHand(), result);
            InvUtils.swapBack();
        }
    }

    public Item getSeeds(Block block) {
        if(block == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if(block == Blocks.CARROTS) return Items.CARROT;
        if(block == Blocks.POTATOES) return Items.POTATO;
        if(block == Blocks.NETHER_WART) return Items.NETHER_WART;
        if(block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        if(block == Blocks.KELP) return Items.KELP;
        if(block == Blocks.KELP_PLANT) return Items.KELP;
        return null;
    }

}
