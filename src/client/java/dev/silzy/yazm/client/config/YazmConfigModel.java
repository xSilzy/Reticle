package dev.silzy.yazm.client.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;
import net.minecraft.client.option.Perspective;

@Modmenu(modId = "yazm")
@Config(name = "yazmConfig", wrapperName = "YazmConfig")
public class YazmConfigModel {
    // General Zoom Settings

    @SectionHeader("zoomSettings")
    public float initZoom = 5f;
    public boolean showZoomLevel = true;

    public float zoomInSpeed = 1f;
    public float zoomOutSpeed = 0.5f;
    public String easingFunction = "easeInOutCubic";

    public boolean toggleZoom = false;
    public boolean hideHud = false;
    public boolean cinematicCam = false;
    public boolean changeMouseSens = true;
    public boolean resetZoom = true;
    public boolean enableResetReminder = true;

    // Scroll Zooming
    @SectionHeader("scrollZooming")
    public boolean scrollZooming = true;
    public float scrollZoomSteps = 0.25f;
    public boolean limitZoom = true;
    public float zoomLimit = 50f;
    public boolean enableZoomOut = true;
    public double thirdPersFrontOffset = -2;
    public double thirdPersBackOffset = 2;
    public Perspective targetPerspective = Perspective.THIRD_PERSON_BACK;
    public boolean limitCamDist = true;
    public double camDistLimit = 8;

    // Smooth scrolling
    public boolean smoothScroll = true;
    public float scrollSmoothness = 10f;
}
