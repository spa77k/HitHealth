package dev.spa.hithealth;

/** Loads the optional expansion only after PlaceholderAPI has been found and enabled. */
final class PlaceholderApiHook {

    private final HitHealthPlaceholderExpansion expansion;

    private PlaceholderApiHook(HitHealthPlaceholderExpansion expansion) {
        this.expansion = expansion;
    }

    static PlaceholderApiHook install(HitHealthPlugin plugin) {
        HitHealthPlaceholderExpansion expansion = new HitHealthPlaceholderExpansion(plugin);
        return expansion.register() ? new PlaceholderApiHook(expansion) : null;
    }

    void unregister() {
        expansion.unregister();
    }
}
