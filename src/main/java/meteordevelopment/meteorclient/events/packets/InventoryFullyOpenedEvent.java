/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.events.packets;

import net.minecraft.inventory.Inventory;

public class InventoryFullyOpenedEvent {
    private static final InventoryFullyOpenedEvent INSTANCE = new InventoryFullyOpenedEvent();

    public Inventory inv;
    public Integer syncId;

    public static InventoryFullyOpenedEvent get(Inventory inv, int syncId) {
        INSTANCE.inv = inv;
        INSTANCE.syncId = syncId;
        return INSTANCE;
    }
}
