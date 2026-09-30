/*
 * This file is part of the YAZM distribution (https://github.com/xSilzy/yet-another-zoom-mod).
 * Copyright (c) 2026 xSilzy.
 * Licensed under GNU GPLv3. See LICENSE for details.
 */

package dev.silzy.reticle.client.util;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import static dev.silzy.reticle.client.core.Reticle.MOD_ID;

public class KeybindUtils {
    private static final String MOD_NAME = MOD_ID;


    public enum KeyCategory {
        ZOOM("zoom");
        /* -- Add more categories if needed -- */

        private final KeyBinding.Category category;

        KeyCategory(String path) {
            this.category = KeyBinding.Category.create(Identifier.of(MOD_NAME.toLowerCase(), path));
        }

        public KeyBinding.Category getCategory() {
            return category;
        }
    }

    // Default overload for keyboard keys
    public static KeyBinding newKeyBind(int keyBind, String keyName, KeyCategory category) {
        return newKeyBind(InputUtil.Type.KEYSYM, keyBind, keyName, category);
    }

    // Full constructor
    public static KeyBinding newKeyBind(InputUtil.Type keyType, int keyBind, String keyName, KeyCategory category) {
        String cleanedName = keyName.toLowerCase().replaceAll("[^a-z0-9/._-]", "-");
        String translationKey = "key." + MOD_NAME.toLowerCase() + "." + cleanedName;

        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey,
                keyType,
                keyBind,
                category.getCategory()
        ));
    }
}
