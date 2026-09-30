/*
 * This file is part of the YAZM distribution (https://github.com/xSilzy/yet-another-zoom-mod).
 * Copyright (c) 2026 xSilzy.
 * Licensed under GNU GPLv3. See LICENSE for details.
 */

package dev.silzy.reticle.client.core;

import dev.silzy.reticle.client.config.reticleConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.silzy.reticle.client.zoom.ZoomHandler.initZoom;

public class Reticle {
    public final static String MOD_ID = "YAZM";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyBinding zoomInKey;
    public static KeyBinding zoomOutKey;

    public static final reticleConfig config = reticleConfig.createAndLoad();
    public static MinecraftClient client;

    public static void initYazm() {
        Reticle.client = MinecraftClient.getInstance();
        if (client == null) {LOGGER.error("Couldn't Initialize Client!");}
        initZoom();
    }

}
