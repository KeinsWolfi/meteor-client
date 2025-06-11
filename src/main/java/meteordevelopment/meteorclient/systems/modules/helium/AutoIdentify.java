/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.packets.ContainerSlotUpdateEvent;
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
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

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
    private boolean allItemsClicked = false;

    private Text title = null;

    private boolean shouldLoot(ScreenHandler handler) {
        String titleString = mc.currentScreen.getTitle().getString();

        if (!(handler instanceof GenericContainerScreenHandler)) return false;

        // INVNAME: 󏿸
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

        List<ItemStack> identified = new ArrayList<>();

        allItemsClicked = false;

        for (int i = 27; i <= SlotUtils.indexToId(SlotUtils.HOTBAR_END); i++) {
            if (!handler.getSlot(i).hasStack()) continue;
            if (!handler.getSlot(i).getStack().getName().getString().startsWith("Unidentified ")) continue;
            if (identified.size() >= 10) break;

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

            identified.add(item.copy());
            InvUtils.shiftClick().slotId(i);
        }

        if (autoIdentify.get()) {
            allItemsClicked = true;
        }
    }

    public void init(GenericContainerScreenHandler handler) {
        MeteorExecutor.execute(() -> moveItems(handler));
    }

    @EventHandler
    private void onInventory(InventoryEvent e) {
        ScreenHandler handler = mc.player.currentScreenHandler;
        if (handler == null) return;

        syncId = e.packet.syncId();

        if (mc.currentScreen == null) {
            clicking = false;
            return;
        };
        title = mc.currentScreen.getTitle();

        try {
            // InvName: 󏿸
            if (shouldLoot(handler) && e.packet.syncId() == handler.syncId && !clicking) {
                // ChatUtils.info("Looting " + title.getString() + "...");
                init((GenericContainerScreenHandler) handler);
                clicking = true;
            }
        } catch (UnsupportedOperationException ex) {
            System.err.println("Unable to construct this menu");
        }
    }

    @EventHandler
    private void onContainerSlotUpdate(ContainerSlotUpdateEvent event) {
        if (allItemsClicked) {
            ScreenHandlerSlotUpdateS2CPacket packet = event.packet;
            if (packet.getSyncId() != syncId) {
                clicking = false;
                allItemsClicked = false;
                return;
            }

            ItemStack newItem = packet.getStack();

            if (newItem.getName().getString().contains("You are identifying")) {
                InvUtils.shiftClick().slotId(packet.getSlot());
            }

            if (newItem.getName().getString().contains("Withdraw Items")) {
                MeteorExecutor.execute(() -> checkItems(mc.player.currentScreenHandler, packet));
            }

            if (newItem.getName().getString().contains("Add items to identify")) {
                //ChatUtils.info("Closing");
                clicking = false;
                allItemsClicked = false;
                mc.execute(() -> mc.player.closeHandledScreen());
            }
        }
    }

    private void checkItems(ScreenHandler handler, ScreenHandlerSlotUpdateS2CPacket packet) {
        //ChatUtils.info("Sleeping");
        try {
            Thread.sleep(101);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //ChatUtils.info("Done");
        ChatUtils.sendMsgWithoutPrefix(Formatting.LIGHT_PURPLE + "Identified items:");
        for (int i = 11; i <= 15; i++){
            HandleItems(handler, i);
        }
        for (int i = 20; i <= 24; i++){
            HandleItems(handler, i);
        }
        //ChatUtils.info("Clicking");
        InvUtils.shiftClick().slotId(packet.getSlot());
    }

    private void HandleItems(ScreenHandler handler, int i) {
        if (handler.getSlot(i).hasStack()) {
            if (!(handler.getSlot(i).getStack().getName().getString().contains("§8§lEmpty Item Slot"))) {
                ChatUtils.sendMsgWithoutPrefix(
                    Text.of(Formatting.GREEN + "    + ").copy()
                        .append(handler.getSlot(i).getStack().getName())
                        .styled(style -> style.withHoverEvent(new HoverEvent.ShowItem(handler.getSlot(i).getStack())))
                );
            }
        }
    }

    private boolean isntEmpty(ItemStack stack) {
        return stack != ItemStack.EMPTY;
    }
}
