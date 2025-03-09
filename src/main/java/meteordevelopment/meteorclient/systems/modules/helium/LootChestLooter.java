/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LootChestLooter extends Module {

    public final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> autoClose = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-close")
            .description("Automatically closes the loot chest after looting.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("delay")
            .description("The delay in milliseconds between each loot.")
            .defaultValue(150)
            .min(50)
            .sliderMax(1000)
            .build()
    );

    public final Setting<Integer> startDelay = sgGeneral.add(new IntSetting.Builder()
            .name("start-delay")
            .description("The delay in milliseconds before starting to loot.")
            .defaultValue(300)
            .min(100)
            .sliderMax(1000)
            .build()
    );

    public LootChestLooter() {
        super(Categories.Helium, "Loot Chest Looter", "Automatically loots Loot Chests.");
    }

    private Integer syncId = null;
    private boolean stealing = false;

    private Text title = null;

    private boolean shouldLoot(ScreenHandler handler) {
        String titleString = mc.currentScreen.getTitle().getString();

        if (!(handler instanceof GenericContainerScreenHandler)) return false;

        return titleString.startsWith("Loot Chest §");
    }

    private void steal(GenericContainerScreenHandler handler) {
        if (mc.currentScreen == null) {
            stealing = false;
            return;
        }

        boolean initial = startDelay.get() != 0;
        Inventory inv = handler.getInventory();
        if (!inv.containsAny(this::isntEmpty)) {
            // ChatUtils.error("Container is empty. Waiting 1 tick then trying again.");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            steal(handler);
            return;
        }

        List<ItemStack> loot = new ArrayList<>();

        for (int i = 0; i <  SlotUtils.indexToId(SlotUtils.MAIN_START); i++) {
            if (!handler.getSlot(i).hasStack()) continue;

            int sleep;
            if (initial) {
                sleep = startDelay.get();
                initial = false;
            } else {
                sleep = delay.get();
            }

            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (mc.currentScreen == null || !Utils.canUpdate()) break;

            ItemStack item = handler.getSlot(i).getStack();
            if (item.getItem() == null) continue;

            loot.add(item.copy());
            InvUtils.shiftClick().slotId(i);
        }

        try {
            Thread.sleep(delay.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (autoClose.get()) {
            mc.execute(() -> {
                mc.player.closeHandledScreen();
            });
            stealing = false;
        }

        ChatUtils.info("Loot from: " + title.getString());
        loot.forEach(item -> ChatUtils.sendMsgWithoutPrefix( "      " + Formatting.GREEN + "+ " + item.getFormattedName().getString() + " x" + item.getCount()));
        loot.clear();
    }

    private void stealInit(GenericContainerScreenHandler handler) {
        MeteorExecutor.execute(() -> steal(handler));
    }

    @EventHandler
    public void onInventory(InventoryEvent e) {
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (handler == null) return;

        syncId = e.packet.getSyncId();

        if (mc.currentScreen == null) {
            stealing = false;
            return;
        };
        title = mc.currentScreen.getTitle();

        try {
            if (shouldLoot(handler) && e.packet.getSyncId() == handler.syncId && !stealing) {
                // ChatUtils.info("Looting " + title.getString() + "...");
                stealInit((GenericContainerScreenHandler) handler);
                stealing = true;
            }
        } catch (UnsupportedOperationException ex) {
            System.err.println("Unable to construct this menu");
        }
    }

    private boolean isntEmpty(ItemStack stack) {
        return stack != ItemStack.EMPTY;
    }
}
