/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.helium;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.Burrow;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class VClip extends Module {

    public final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<ClipMode> mode = sgGeneral.add(new EnumSetting.Builder<ClipMode>()
        .name("Mode")
        .description("Mode. Note: Spigot mode only works on some servers and doesnt bypass Vanilla AntiCheat.")
        .defaultValue(ClipMode.Vanilla)
        .build()
    );

    public final Setting<DistMode> distmode = sgGeneral.add(new EnumSetting.Builder<DistMode>()
        .name("Distance Mode")
        .description("How you want to clip. NOTE: NextUP and NextDOWN will use the height setting as the maximum distance.")
        .defaultValue(DistMode.NextUP)
        .build()
    );

    public final Setting<Double> height = sgGeneral.add(new DoubleSetting.Builder()
            .name("height")
            .description("How much to clip you up.")
            .defaultValue(2)
            .sliderMin((mode.get().equals(ClipMode.Vanilla) ? -10 : mode.get().equals(ClipMode.Spigot) ? -100 : -10))
            .sliderMax(mode.get().equals(ClipMode.Vanilla) ? 10 : mode.get().equals(ClipMode.Spigot) ? 100 : 10)
            .decimalPlaces(1)
            .build()
    );

    public VClip() {
        super(Categories.Helium, "VClip", "Clips you up/down through blocks.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;
        if(distmode.get().equals(DistMode.Custom)) {
            clip(mode.get(), height.get());
        }
        else if(distmode.get().equals(DistMode.NextUP)){
            Vec3d pos = mc.player.getPos();
            Vec3d finPos = new Vec3d(pos.x, pos.y + height.get(), pos.z);
            BlockHitResult result = mc.world.raycast(new RaycastContext(finPos, finPos.subtract(0, height.get(), 0), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            BlockPos resultPos = result.getBlockPos().up();

            boolean canClip = checkBlock(resultPos);

            if(canClip){
                clip(mode.get(), resultPos.getY()-pos.getY());
            }
        } else if (distmode.get().equals(DistMode.NextDOWN)) {
            /*
            Vec3d playerPos = mc.player.getPos();
            BlockPos startPos = new BlockPos((int) Math.floor(playerPos.x), (int) Math.floor(playerPos.y - 1), (int) Math.floor(playerPos.z));

            for (int i = 0; i < height.get(); i++) {
                BlockPos currentPos = new BlockPos(startPos.getX(), startPos.getY() - i, startPos.getZ());

                if(mc.world.getBlockState(currentPos).isAir()){
                    if(mc.world.getBlockState(new BlockPos(currentPos.getX(), currentPos.getY() - 1, currentPos.getZ())).isAir()){}
                    else{
                        if(checkBlock(currentPos)){
                            clip(mode.get(), -i);
                            return;
                        }
                    }
                }
            }
            */
        }
        toggle();
    }

    private boolean checkBlock(BlockPos resultPos) {
        if (mc.world.getBlockState(new BlockPos(resultPos.getX(), resultPos.getY() - 1, resultPos.getZ())).isAir()) {
            return false;
        } else if (!mc.world.getBlockState(new BlockPos(resultPos.getX(), resultPos.getY(), resultPos.getZ())).isAir()) {
            return false;
        } else
            return mc.world.getBlockState(new BlockPos(resultPos.getX(), resultPos.getY() + 1, resultPos.getZ())).isAir();
    }

    public void clip(ClipMode mode, double height) {
        if (mc.player == null) return;
        if (mode.equals(ClipMode.Spigot)) {
            int packetsToSend = (int) Math.ceil(height / 10);

            for (int i = 0; i < packetsToSend; i++) {
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround(), mc.player.horizontalCollision));
            }
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + height, mc.player.getZ(), mc.player.isOnGround(), mc.player.horizontalCollision));
            mc.player.updatePosition(mc.player.getX(), mc.player.getY() + height, mc.player.getZ());
        } else if (mode.equals(ClipMode.Vanilla)) {
            mc.player.updatePosition(mc.player.getX(), mc.player.getY() + height, mc.player.getZ());
        }
    }

    public enum ClipMode{
        Vanilla,
        Spigot
    }

    public enum DistMode {
        NextUP,
        NextDOWN,
        Custom
    }

}
