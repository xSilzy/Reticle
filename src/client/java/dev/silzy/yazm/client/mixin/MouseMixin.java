/*
 * This file is part of the YAZM distribution (https://github.com/xSilzy/yet-another-zoom-mod).
 * Copyright (c) 2026 xSilzy.
 * Licensed under GNU GPLv3. See LICENSE for details.
 */

package dev.silzy.yazm.client.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.silzy.yazm.client.zoom.ZoomHandler.*;
import static java.lang.Math.clamp;

@Mixin(Mouse.class)
public class MouseMixin {
    @Redirect(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            )
    )
    private void decreaseMouseSens(ClientPlayerEntity player, double x, double y) {
        float mouseScaling = getMouseScaling();
        mouseScaling = clamp(mouseScaling, 0, 150_000); // too high values cause glitching
        if (mouseScaling != 1){
            x = x / mouseScaling;
            y = y / mouseScaling;
        }

        player.changeLookDirection(x, y);

    }


    @Inject(
            method = "onMouseScroll",
            at = @At("HEAD"),
            cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo info) {
        if (scrollZoom((float) vertical)){
            info.cancel();
        }
    }
}
