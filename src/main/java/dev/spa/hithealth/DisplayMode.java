package dev.spa.hithealth;

/** Where a hit's health text is sent. */
public enum DisplayMode {
    ACTIONBAR,
    BOSSBAR,
    BOTH;

    public boolean showsActionBar() {
        return this != BOSSBAR;
    }

    public boolean showsBossBar() {
        return this != ACTIONBAR;
    }
}
