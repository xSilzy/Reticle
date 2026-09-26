/*
 * This file is part of the YAZM distribution (https://github.com/xSilzy/yet-another-zoom-mod).
 * Copyright (c) 2026 xSilzy.
 * Licensed under GNU GPLv3. See LICENSE for details.
 */

package dev.silzy.yazm.client.math;

import java.util.HashMap;
import java.util.Map;
import static dev.silzy.yazm.client.core.Yazm.LOGGER;

public class EasingHelper {
    private static final Map<String, EasingFunction> FUNCTIONS = new HashMap<>();
    private static final EasingFunction defaultFunc = t -> t;

    // runs when class is loaded
    static {
        FUNCTIONS.put("easeInOutQuadratic", t -> t < 0.5 ? 2 * t * t : (float) (1 - Math.pow(-2 * t + 2, 2) / 2)); // Ease in out Quadratic
        FUNCTIONS.put("easeInOutCubic", t -> t < 0.5 ? 4 * t * t * t : (float) (1 - Math.pow(-2 * t + 2, 3) / 2)); // Ease in out cubic
        FUNCTIONS.put("easeInOutQuartic", t -> t < 0.5 ? 8 * t * t * t * t : (float) (1 - Math.pow(-2 * t + 2, 4) / 2)); // Ease in out Quartic
        FUNCTIONS.put("easeOutElastic", t -> {
            float c4 = (float) ((2 * Math.PI) / 3);
            return (float) (Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1);
        }); // Ease out Elastic
    }

    public static EasingFunction getFunc(String functionName){
        EasingFunction func = FUNCTIONS.getOrDefault(functionName, defaultFunc);
        if (func == defaultFunc) LOGGER.warn("'{}' not found using default func", functionName);
        return func;
    }

}
