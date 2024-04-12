/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class CUM extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Item> item = sgGeneral.add(new ItemSetting.Builder()
        .name("Items")
        .description("Item to throw")
        .defaultValue(Items.SNOWBALL)
        .build()
    );

    public final Setting<Integer> ammount = sgGeneral.add(new IntSetting.Builder()
        .name("Ammount")
        .description("Ammount of packets")
        .defaultValue(16)
        .min(1)
        .max(64)
        .sliderMin(1)
        .sliderMax(64)
        .build()
    );

    public CUM() {
        super(Categories.Helium, "CUM", "Makes you CUM");
    }

    @Override
    public void onActivate() {
        if(mc.player == null) return;
        for (int i = 0; i < ammount.get(); i++) {
            FindItemResult items = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == item.get());
            if(!items.found()){
                ChatUtils.error("You dont have any(more) of your selected item in your hotbar.");
                toggle();
                return;
            }
            InvUtils.swap(items.slot(), true);
            useItem(items);
            InvUtils.swapBack();
        }
        toggle();
    }

    private void useItem(FindItemResult item) {
        if(item.isOffhand()){
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
        } else {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }

}
