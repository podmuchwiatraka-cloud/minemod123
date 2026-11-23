package pl.minemod.modules;

import pl.minemod.config.Config;

public final class MineModule {
    public enum Category {
        HUD("HUD"), VISUAL("VISUAL");
        public final String label;
        Category(String label) { this.label = label; }
    }

    public final String id;
    public final String name;
    public final String tag;
    public final Category category;
    public final String[] options;
    private final boolean defaultEnabled;

    public MineModule(String id, String name, String tag, Category category,
                      boolean defaultEnabled, String... options) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.category = category;
        this.defaultEnabled = defaultEnabled;
        this.options = options;
    }

    public boolean isEnabled() {
        return Config.get().enabled.getOrDefault(id, defaultEnabled);
    }

    public void toggle() {
        Config.get().enabled.put(id, !isEnabled());
        Config.save();
    }

    public int option() {
        int o = Config.get().option.getOrDefault(id, 0);
        return (o < 0 || o >= options.length) ? 0 : o;
    }

    public String optionLabel() { return options[option()]; }

    public void cycleOption() {
        Config.get().option.put(id, (option() + 1) % options.length);
        Config.save();
    }
}
