/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.misc;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.InventoryFullyOpenedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

public class InventoryDataHandler {
    private Inventory currentInventory;
    private int currentSyncId;
    private boolean isFullyOpened = false;
    private boolean isScreenOpened = false;

    public void setInventory(Inventory inv, int syncId) {
        currentInventory = inv;
        currentSyncId = syncId;
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive e) {
        Packet<?> packet = e.packet;
        if (packet instanceof InventoryS2CPacket) {
            currentSyncId = ((InventoryS2CPacket) packet).getSyncId();

            for (int i = 0; i < ((InventoryS2CPacket) packet).getContents().size(); i++) {
                currentInventory.setStack(i, ((InventoryS2CPacket) packet).getContents().get(i));

                System.out.println("Slot " + i + ": " + ((InventoryS2CPacket) packet).getContents().get(i) + " Sync ID: " + currentSyncId);
            }

            isScreenOpened = true;
        }

        if (packet instanceof ScreenHandlerSlotUpdateS2CPacket screenPacket && isScreenOpened) {
            if (screenPacket.getSyncId() != currentSyncId) return;
            int slot = screenPacket.getSlot();
            ItemStack newStack = screenPacket.getStack();
            currentInventory.setStack(slot, newStack);

            if (!(currentInventory.containsAny(this::isEmpty))) {
                isFullyOpened = true;
                MeteorClient.EVENT_BUS.post(InventoryFullyOpenedEvent.get(currentInventory, currentSyncId));
            }
        }
    }

    private boolean isEmpty(ItemStack stack) {
        return stack == ItemStack.EMPTY;
    }
}
