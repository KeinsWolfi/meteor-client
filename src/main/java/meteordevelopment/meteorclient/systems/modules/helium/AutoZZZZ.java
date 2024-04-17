/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

import java.util.Random;

public class AutoZZZZ extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final SettingGroup sgSnore = settings.createGroup("Snore");

    private final SettingGroup sgRandom = settings.createGroup("Random");

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between ZZZZ in ticks.")
        .defaultValue(100)
        .min(0)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> ammount = sgGeneral.add(new IntSetting.Builder()
        .name("ammount")
        .description("The ammount of z's to send.")
        .defaultValue(5)
        .min(1)
        .sliderMax(100)
        .build()
    );

    private final Setting<Boolean> random = sgRandom.add(new BoolSetting.Builder()
        .name("randomise")
        .description("Randomise the capitalisation of the z's.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> snore = sgSnore.add(new BoolSetting.Builder()
        .name("snore")
        .description("Adds rrrrrrrrrr before the z's.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> snoreAmount = sgSnore.add(new IntSetting.Builder()
        .name("snore-amount")
        .description("The amount of r's to send.")
        .defaultValue(5)
        .min(1)
        .sliderMax(100)
        .visible(snore::get)
        .build()
    );

    private final Setting<Integer> randomZZZ = sgRandom.add(new IntSetting.Builder()
        .name("random-amount (+/-)")
        .description("The randomization amount of z's to send.")
        .defaultValue(0)
        .min(0)
        .sliderMax(100)
        .visible(random::get)
        .build()
    );

    private final Setting<Integer> randomRRR = sgRandom.add(new IntSetting.Builder()
        .name("random-amount (+/-)")
        .description("The randomization amount of z's to send.")
        .defaultValue(0)
        .min(0)
        .sliderMax(100)
        .visible(random::get)
        .build()
    );

    public AutoZZZZ() {
        super(Categories.Helium, "AutoZZZZ", "Automatically ZZZZ in bed.");
    }

    private int timer;

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Random randomnum = new Random();
        String text = "";
        if(!mc.player.isSleeping()) return;
        if (timer <= 0) {
            if(snore.get()){
                text = "r".repeat((int) (snoreAmount.get() + Math.round(randomRRR.get() * (randomnum.nextDouble() - 0.5d))));
            }
            text = text + "z".repeat((int) (ammount.get() + Math.round(randomZZZ.get() * (randomnum.nextDouble() - 0.5d))));

            if(random.get()){
                text = randomizeCapitals(text);
            }

            ChatUtils.sendPlayerMsg(text);
            timer = delay.get();
        }
        else {
            timer--;
        }
    }

    public String randomizeCapitals(String text) {
        if(text == null || text.isEmpty()) return text;
        StringBuilder newText = new StringBuilder();

        Random random = new Random();

        for(char c : text.toCharArray()) {
            if (random.nextBoolean()) {
                newText.append(Character.toUpperCase(c));
            } else {
                newText.append(c);
            }
        }
        return newText.toString();
    }

}
