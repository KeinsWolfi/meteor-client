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
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.regex.Pattern;

public class AutoIdentify extends Module {

    public final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> autoIdentify = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-close")
        .description("Automatically closes the loot chest after looting.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay in milliseconds between each click.")
        .defaultValue(150)
        .min(50)
        .sliderMax(1000)
        .build()
    );

    public final Setting<Integer> startDelay = sgGeneral.add(new IntSetting.Builder()
        .name("start-delay")
        .description("The delay in milliseconds before starting to identify.")
        .defaultValue(300)
        .min(100)
        .sliderMax(1000)
        .build()
    );

    public AutoIdentify() {
        super(Categories.Helium, "auto-identify", "Automatically identifies items in your inventory.");
    }

    private Integer syncId = null;
    private boolean clicking = false;

    private Text title = null;

    private boolean shouldLoot(ScreenHandler handler) {
        String titleString = mc.currentScreen.getTitle().getString();

        if (!(handler instanceof GenericContainerScreenHandler)) return false;

        System.out.println(titleString);

        // INVNAME: 󏿸
        ChatUtils.error("Title: " + titleString + titleString.contains("\uDAFF\uDFF8\uE018"));
        return titleString.contains("\uDAFF\uDFF8\uE018");
    }

    public void moveItems(GenericContainerScreenHandler handler) {
        if (mc.currentScreen == null) {
            clicking = false;
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
            moveItems(handler);
            return;
        }

        for (int i = 0; i < SlotUtils.indexToId(SlotUtils.HOTBAR_START); i++) {
            ChatUtils.sendMsgWithoutPrefix("Moving item " + handler.getSlot(i).getStack().getFormattedName() + "... (" + i + ")");
            System.out.println(handler.getSlot(i).getStack().getFormattedName());
        }
    }

    public void init(GenericContainerScreenHandler handler) {
        MeteorExecutor.execute(() -> moveItems(handler));
    }

    @EventHandler
    private void onInventory(InventoryEvent e) {
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (handler == null) return;

        syncId = e.packet.getSyncId();

        if (mc.currentScreen == null) {
            clicking = false;
            return;
        };
        title = mc.currentScreen.getTitle();

        try {
            // InvName: 󏿸
            if (shouldLoot(handler) && e.packet.getSyncId() == handler.syncId && !clicking) {
                // ChatUtils.info("Looting " + title.getString() + "...");
                init((GenericContainerScreenHandler) handler);
                clicking = true;
            }
        } catch (UnsupportedOperationException ex) {
            System.err.println("Unable to construct this menu");
        }
    }

    private boolean isntEmpty(ItemStack stack) {
        return stack != ItemStack.EMPTY;
    }
}
