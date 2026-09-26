/*
 * This file is part of the YAZM distribution (https://github.com/xSilzy/yet-another-zoom-mod).
 * Copyright (c) 2026 xSilzy.
 * Licensed under GNU GPLv3. See LICENSE for details.
 */

package dev.silzy.yazm.client;

import net.fabricmc.api.ClientModInitializer;

import static dev.silzy.yazm.client.core.Yazm.initYazm;

public class YazmClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        initYazm();
    }
}
