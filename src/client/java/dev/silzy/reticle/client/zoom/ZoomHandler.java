/*
 * This file is part of the YAZM distribution (https://github.com/xSilzy/yet-another-zoom-mod).
 * Copyright (c) 2026 xSilzy.
 * Licensed under GNU GPLv3. See LICENSE for details.
 */

package dev.silzy.reticle.client.zoom;

import dev.silzy.reticle.client.math.EasingFunction;
import dev.silzy.reticle.client.math.EasingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;

import static dev.silzy.reticle.client.core.Reticle.*;
import static dev.silzy.reticle.client.util.ClientUtils.getDeltaTime;
import static dev.silzy.reticle.client.util.ClientUtils.sendActionBarMessage;
import static dev.silzy.reticle.client.util.KeybindUtils.KeyCategory;
import static dev.silzy.reticle.client.util.KeybindUtils.newKeyBind;
import static java.lang.Math.abs;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.lerp;

public class ZoomHandler {

    private static final Identifier MODIFIER_ID = Identifier.of(MOD_ID.toLowerCase(), "camera_distance_modifier");
    private static final double  MIN_CAM_DIST = 0.5;

    private static float interpolant;
    private static boolean isZooming;
    private static boolean isToggle;
    private static boolean isUsingSpyglass;
    private static boolean isZoomOut;
    private static boolean needsReset;
    private static boolean preHidden;
    private static boolean preCinematic;
    private static Perspective prePerspective;
    private static float zoomFactor;
    private static float targetZoomFactor;
    private static double addedCamDist = -1;
    private static double targetAddedCamDist;
    private static double camDistOffset;


    public static void initZoom(){
        zoomFactor =  config.initZoom();
        targetZoomFactor = zoomFactor;
        camDistOffset = -(getBaseCamDist() + MIN_CAM_DIST);
        addedCamDist = abs(camDistOffset);
        targetAddedCamDist = addedCamDist;

        zoomInKey = newKeyBind(InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_Z, "zoom_in", KeyCategory.ZOOM);
        zoomOutKey = newKeyBind(InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_X, "zoom_out", KeyCategory.ZOOM);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean prevZooming = isZooming;
            isZooming = canZoom();
            if (isZooming != prevZooming) {
                if (isZooming) onActivate();
                else onDeactivate();
            }

            if (client.player != null){
                isUsingSpyglass = client.player.isUsingSpyglass();

                // disable zoom when in GUI's
                if (client.currentScreen != null) {
                    isToggle = false;
                    isZoomOut = false;
                    onDeactivate();
                }
            }

            syncZoomState();
            if (config.showZoomLevel() && (isZooming || isToggle)) showZoomLevel();
            if (isZooming && zoomFactor <= 1.5 && !config.resetZoom() && config.enableResetReminder() && !(isUsingSpyglass || isZoomOut || needsReset)) {
                Text warnMessage = Text.literal("Zoom level < 1.5").formatted(Formatting.RED);
                sendActionBarMessage(warnMessage);
            }
        });
    }


    private static boolean canZoom() {
        boolean isPlaying = client != null
                && client.world != null
                && client.player != null
                && client.player.isAlive()
                && client.currentScreen == null;


        return isPlaying && zoomInKey.isPressed() || isPlaying && zoomOutKey.isPressed() || isPlaying && isUsingSpyglass;
    }

    public static float updateFov(float fov){
        if (!isZooming && !isToggle && interpolant == 0 && !needsReset) return fov;
        return fov / getZoomFactor();
    }

    private static void updateInterpolant(){
        float deltaTime = getDeltaTime();
        float zoomDirection = (isZooming || isToggle ? 1 : -1);
        float zoomSpeed = (isZooming ? config.zoomInSpeed() : config.zoomOutSpeed());

        interpolant += (deltaTime  * (1 / zoomSpeed)) * zoomDirection;
        interpolant = clamp(interpolant, 0, 1);
    }

    private static float getZoomFactor(){
        EasingFunction easingFunc = EasingHelper.getFunc(config.easingFunction());
        updateInterpolant();
        if (isZoomOut || needsReset) {
            updateCamDistance();

            // lwk idk if this can be written shorter but it works so i'll just ball with it
            boolean isNotZooming = !(isZooming || isToggle) && interpolant <= 0 || needsReset && interpolant >= 1;
            boolean isAtMinDist = isDistanceNear(getCamDist(), MIN_CAM_DIST) || (!prePerspective.isFirstPerson() && !(isZooming || isToggle) && isDistanceNear(getCamDist(), getBaseCamDist()));
            if (isNotZooming && isAtMinDist) {
                if (isDistanceNear(getCamDist(), MIN_CAM_DIST)) resetZoomState(Perspective.FIRST_PERSON);
                else resetZoomState(prePerspective);
            }

            return 1;
        }

        if (zoomFactor != targetZoomFactor && config.smoothScroll()){
            zoomFactor = (float) smoothLerp(zoomFactor, targetZoomFactor);
        }
        return lerp(easingFunc.ease(interpolant), 1, zoomFactor);
    }

    public static void updateCamDistance(){
        EasingFunction easingFunc = EasingHelper.getFunc(config.easingFunction());
        if (!Objects.equals(addedCamDist, targetAddedCamDist) && config.smoothScroll()) {
            addedCamDist = smoothLerp(addedCamDist, targetAddedCamDist);
        }

        double start = !prePerspective.isFirstPerson() ? getBaseCamDist() + MIN_CAM_DIST : 1;
        addedCamDist = lerp(easingFunc.ease(interpolant), start, addedCamDist);
        camDistOffset = -(getBaseCamDist() + MIN_CAM_DIST);
        applyCamModifier(camDistOffset + addedCamDist);
    }

    private static void applyCamModifier(double val) {
        EntityAttributeInstance attr = getCamAttr();
        if (attr == null) return;

        if (attr.hasModifier(MODIFIER_ID)) {
            attr.removeModifier(MODIFIER_ID);
        }
        attr.addTemporaryModifier(new EntityAttributeModifier(
                MODIFIER_ID,
                val,
                EntityAttributeModifier.Operation.ADD_VALUE
        ));
    }

    private static void clearCamModifier(){
        EntityAttributeInstance attr = getCamAttr();
        if (attr == null) return;

        if (attr.hasModifier(MODIFIER_ID)) {
            attr.removeModifier(MODIFIER_ID);
        }
    }

    private static double getCamDist() {
        EntityAttributeInstance attr = getCamAttr();
        return attr != null ? attr.getValue() : 4.0; // 4.0 is the default value as of the time of writing this
    }

    private static double getBaseCamDist() {
        EntityAttributeInstance attr = getCamAttr();
        return attr != null ? attr.getBaseValue() : 4.0; // 4.0 is the default value as of the time of writing this
    }

    private static EntityAttributeInstance getCamAttr() {
        if (client.player != null) {
            return client.player.getAttributeInstance(EntityAttributes.CAMERA_DISTANCE);
        }
        return null;
    }

    public static void syncZoomState(){
        if (!isZooming && !isToggle || isUsingSpyglass) return;
        Perspective currentPerspective = client.options.getPerspective();
        boolean prevZoomOut = isZoomOut;
        isZoomOut = switch (currentPerspective){
            case Perspective.FIRST_PERSON: {
                yield config.enableZoomOut() && targetZoomFactor <= 1;
            }
            case Perspective.THIRD_PERSON_BACK, Perspective.THIRD_PERSON_FRONT: {
                double distToCheck = (!prePerspective.isFirstPerson() && (isZooming || isToggle)) || prePerspective.isFirstPerson() ? MIN_CAM_DIST : getBaseCamDist();
                yield config.enableZoomOut() && getCamDist() >= distToCheck;
            }
        };

        if (isZoomOut != prevZoomOut) {
            if (isZoomOut){
                needsReset = true;
                interpolant = 0;

                double thirdPersOffset = switch (currentPerspective){
                    case Perspective.FIRST_PERSON: {
                        yield 0;
                    }
                    case Perspective.THIRD_PERSON_FRONT: {
                        yield config.thirdPersFrontOffset();

                    }
                    case Perspective.THIRD_PERSON_BACK: {
                        yield config.thirdPersBackOffset();
                    }
                };

                if (config.resetZoom() || (targetZoomFactor > 1) ) {
                    addedCamDist = abs(camDistOffset) + thirdPersOffset;
                    targetAddedCamDist = addedCamDist;
                }
                if (currentPerspective.isFirstPerson()) client.options.setPerspective(config.targetPerspective());
            }else {
                resetZoomState(prePerspective);
            }
        }
    }

    private static void resetZoomState() {
        isZoomOut = false;
        needsReset = false;
        interpolant = 0;
        clearCamModifier();
        zoomFactor = 1.f;
        targetZoomFactor = 1.1f;

        if (config.resetZoom()){
            addedCamDist = abs(camDistOffset);
            targetAddedCamDist = addedCamDist;
        }

    }

    private static void resetZoomState(Perspective perspective) {
        client.options.setPerspective(perspective);
        resetZoomState();
    }

    private static double smoothLerp(double baseVal, double targetVal){
        float deltaTime = getDeltaTime();
        return lerp(clamp(config.scrollSmoothness() * deltaTime, 0 ,1), baseVal, targetVal);
    }

    public static boolean scrollZoom(float scrollDirection){
        if ((isZooming || isToggle) && config.scrollZooming()) {
            if (!isZoomOut) {
                targetZoomFactor *= 1 + (config.scrollZoomSteps() * scrollDirection);
                if (targetZoomFactor < 1) targetZoomFactor = 1;
                if (config.limitZoom()) targetZoomFactor = clamp(targetZoomFactor, 1, config.zoomLimit());
            } else {
                targetAddedCamDist *= 1 + (config.scrollZoomSteps() * -scrollDirection); // inverted scroll direction since scrolling down now zooms out
                if (targetAddedCamDist < 1) targetAddedCamDist = 1;
                if (config.limitCamDist()) targetAddedCamDist = clamp(targetAddedCamDist, 1, config.camDistLimit() + MIN_CAM_DIST);
            }

            if (!config.smoothScroll()){
                zoomFactor = targetZoomFactor;
                addedCamDist = targetAddedCamDist;
            }
            return true;
        }
        return false;
    }

    public static float getMouseScaling(){
        if (!config.changeMouseSens() || isZoomOut) return 1;
        float currentFov = zoomFactor;

        EasingFunction easingFunc = EasingHelper.getFunc(config.easingFunction());
        return lerp(easingFunc.ease(interpolant), 1, currentFov);
    }

    private static void onActivate(){
        float initZoom = config.initZoom();
        boolean resetZoom = config.resetZoom();

        if (!isToggle) {
            if (zoomFactor != initZoom && resetZoom) {
                zoomFactor = initZoom;
                interpolant = 0;
                clearCamModifier();
            }

            if (isUsingSpyglass && resetZoom) zoomFactor = 1;
            if (zoomOutKey.isPressed()) zoomFactor = 1;
            targetZoomFactor = zoomFactor;

            preHidden = client.options.hudHidden;
            if (config.hideHud() && !preHidden) {
                preHidden = false;
                client.options.hudHidden = true;
            }

            preCinematic = client.options.smoothCameraEnabled;
            if (config.cinematicCam() && !preCinematic) {
                preCinematic = false;
                client.options.smoothCameraEnabled = true;
            }

            if (!isZoomOut && !needsReset) {
                prePerspective = client.options.getPerspective();
            }
        }

        isToggle = config.toggleZoom() && !isUsingSpyglass ?  !isToggle : false;
    }

    private static void onDeactivate(){
        if (!isToggle) {
            if (!preHidden && config.hideHud()) {
                client.options.hudHidden = false;
            }

            if (!preCinematic && config.cinematicCam()){
                client.options.smoothCameraEnabled = false;
            }
        }
    }

    private static boolean isDistanceNear(double current, double target){
        return abs(current - target) < 0.01;
    }

    private static void showZoomLevel(){
        if (isZoomOut) sendActionBarMessage(Text.literal("Zooming Out: " + (float) Math.round(getCamDist() * 10)/10 + " Blocks"));
        else sendActionBarMessage(Text.literal("Zoom: " + (float) Math.round(zoomFactor * 10)/10 + "x"));
    }
}