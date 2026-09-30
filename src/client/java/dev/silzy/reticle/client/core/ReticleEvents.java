/*
 * This file is part of the YAZM distribution (https://github.com/xSilzy/yet-another-zoom-mod).
 * Copyright (c) 2026 xSilzy.
 * Licensed under GNU GPLv3. See LICENSE for details.
 */

package dev.silzy.reticle.client.core;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class ReticleEvents {

    public static final Event<OnActivate> ON_ACTIVATE = EventFactory.createArrayBacked(OnActivate.class,
            (listeners) -> () -> {
                for (OnActivate listener : listeners) {
                    listener.onActivate();
                }
            }
        );

    public static final Event<OnDeactivate> ON_DEACTIVATE = EventFactory.createArrayBacked(OnDeactivate.class,
            (listeners) -> () -> {
                for (OnDeactivate listener : listeners) {
                    listener.OnDeactivate();
                }
            }
        );


    @FunctionalInterface
    public interface OnActivate {
        void onActivate();
    }

    @FunctionalInterface
    public interface OnDeactivate {
        void OnDeactivate();
    }
}
