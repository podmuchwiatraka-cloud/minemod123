package pl.minemod.cosmetics;

import pl.minemod.config.Config;

import java.util.ArrayList;
import java.util.List;

/** Lista kosmetykow + wybor gracza (zapisywany w configu). */
public final class CosmeticRegistry {
    private static final List<Cosmetic> ALL = new ArrayList<>();

    private CosmeticRegistry() {}

    public static void init() {
        register(new Cosmetic("cape_classic", "Klasyczna pelerynka", CosmeticType.CAPE));
        register(new Cosmetic("cape_minimal", "Minimal (szara)", CosmeticType.CAPE));
        register(new Cosmetic("cape_white", "Biala pelerynka", CosmeticType.CAPE));
        register(new Cosmetic("cape_dark", "Ciemna pelerynka", CosmeticType.CAPE));
        register(new Cosmetic("backpack_basic", "Plecak podstawowy", CosmeticType.BACKPACK));
        register(new Cosmetic("backpack_big", "Duzy plecak", CosmeticType.BACKPACK));
        register(new Cosmetic("hat_cap", "Czapka z daszkiem", CosmeticType.HAT));
        register(new Cosmetic("hat_halo", "Aureola", CosmeticType.HAT));
        register(new Cosmetic("wings_white", "Biale skrzydla", CosmeticType.WINGS));
        register(new Cosmetic("wings_dark", "Ciemne skrzydla", CosmeticType.WINGS));
        register(new Cosmetic("wings_angel", "Anielskie skrzydla", CosmeticType.WINGS));
        register(new Cosmetic("pet_cube", "Kostka-zwierzak", CosmeticType.PET));
    }

    public static void register(Cosmetic c) { ALL.add(c); }

    public static List<Cosmetic> all() { return ALL; }

    public static List<Cosmetic> byType(CosmeticType type) {
        return ALL.stream().filter(c -> c.type() == type).toList();
    }

    public static boolean isEquipped(Cosmetic c) {
        return c.id().equals(Config.get().equipped.get(c.type().name()));
    }

    public static Cosmetic equipped(CosmeticType type) {
        String id = Config.get().equipped.get(type.name());
        if (id == null) return null;
        return ALL.stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
    }

    /** Klikniecie w ten sam kosmetyk zdejmuje go. */
    public static void toggle(Cosmetic c) {
        if (isEquipped(c)) Config.get().equipped.remove(c.type().name());
        else Config.get().equipped.put(c.type().name(), c.id());
        Config.save();
    }
}
