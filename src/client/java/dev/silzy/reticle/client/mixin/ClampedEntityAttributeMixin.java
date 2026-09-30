package dev.silzy.reticle.client.mixin;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClampedEntityAttribute.class)
public class ClampedEntityAttributeMixin {
    @Mutable @Shadow @Final
    private double maxValue;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    public void getFov(String translationKey, double fallback, double min, double max, CallbackInfo ci){
        if (translationKey.equals("attribute.name.camera_distance")) {
            this.maxValue = Double.MAX_VALUE; // basically removes the clamp
        }
    }
}
