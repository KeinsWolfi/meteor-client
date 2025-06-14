/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.systems.modules.render.ItemHighlight;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.message.SentMessage;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {
    @Shadow
    protected Slot focusedSlot;

    @Shadow
    protected int x;
    @Shadow
    protected int y;

    @Shadow
    @Nullable
    protected abstract Slot getSlotAt(double xPosition, double yPosition);

    @Shadow
    public abstract T getScreenHandler();

    @Shadow
    private boolean doubleClicking;

    @Shadow
    protected abstract void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType);

    @Shadow
    public abstract void close();

    @Unique
    private static final ItemStack[] ITEMS = new ItemStack[27];

    public HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        InventoryTweaks invTweaks = Modules.get().get(InventoryTweaks.class);

        if (invTweaks.isActive() && invTweaks.showButtons() && invTweaks.canSteal(getScreenHandler())) {
            addDrawableChild(
                new ButtonWidget.Builder(Text.literal("Steal"), button -> invTweaks.steal(getScreenHandler()))
                    .position(x, y - 22)
                    .size(40, 20)
                    .build()
            );

            addDrawableChild(
                new ButtonWidget.Builder(Text.literal("Dump"), button -> invTweaks.dump(getScreenHandler()))
                    .position(x + 42, y - 22)
                    .size(40, 20)
                    .build()
            );

            addDrawableChild(
                new ButtonWidget.Builder(Text.literal("Copy Name"), button -> {
                    StringSelection stringSelection = new StringSelection(mc.currentScreen.getTitle().getString());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
                    ChatUtils.sendMsgWithoutPrefix("Copied name to clipboard. " + mc.currentScreen.getTitle().getString());
                })
                    .position(x + 84, y - 22)
                    .size(40, 20)
                    .build()
            );

            addDrawableChild(
                new ButtonWidget.Builder(Text.literal("Say Items"), button -> {
                    ChatUtils.sendMsgWithoutPrefix("Items in container: ");
                    for (Slot slot : getScreenHandler().slots) {
                        if (slot.hasStack()) {
                            ChatUtils.sendMsgWithoutPrefix(
                                Text.literal(slot.id + ": [")
                                    .append(slot.getStack().getName())
                                    .styled(style -> style.withHoverEvent(new HoverEvent.ShowItem(slot.getStack())))
                                    .append("] in (" + getScreenHandler().getType().toString() + ")")
                            );
                        }
                    }
                })
                    .position(x + 126, y - 22)
                    .size(40, 20)
                    .build()
            );
        }
    }

    // Inventory Tweaks
    @Inject(method = "mouseDragged", at = @At("TAIL"))
    private void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> info) {
        if (button != GLFW_MOUSE_BUTTON_LEFT || doubleClicking || !Modules.get().get(InventoryTweaks.class).mouseDragItemMove()) return;

        Slot slot = getSlotAt(mouseX, mouseY);
        if (slot != null && slot.hasStack() && hasShiftDown()) onMouseClick(slot, slot.id, button, SlotActionType.QUICK_MOVE);
    }

    // Middle click open
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        BetterTooltips tooltips = Modules.get().get(BetterTooltips.class);

        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && focusedSlot != null && !focusedSlot.getStack().isEmpty() && mc.player.currentScreenHandler.getCursorStack().isEmpty() && tooltips.middleClickOpen()) {
            ItemStack itemStack = focusedSlot.getStack();
            if (Utils.hasItems(itemStack) || itemStack.getItem() == Items.ENDER_CHEST) {
                cir.setReturnValue(Utils.openContainer(focusedSlot.getStack(), ITEMS, false));
            }
            else if (itemStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT) != null || itemStack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT) != null) {
                close();
                mc.setScreen(new BookScreen(BookScreen.Contents.create(itemStack)));
                cir.setReturnValue(true);
            }
        }
    }

    // Item Highlight
    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        int color = Modules.get().get(ItemHighlight.class).getColor(slot.getStack());
        if (color != -1) context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color);
    }
}
