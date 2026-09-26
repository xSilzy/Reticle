/*
 * This file is part of the YAZM distribution (https://github.com/xSilzy/yet-another-zoom-mod).
 * Copyright (c) 2026 xSilzy.
 * Licensed under GNU GPLv3. See LICENSE for details.
 */

package dev.silzy.yazm.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ClientUtils {

    public static void sendActionBarMessage(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(message, true);
    }
    public static float getDeltaTime() {
        return MinecraftClient.getInstance().getRenderTickCounter().getFixedDeltaTicks() / 20f;
    }
}
