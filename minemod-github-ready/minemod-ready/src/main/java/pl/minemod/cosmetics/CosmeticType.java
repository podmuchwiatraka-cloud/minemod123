package pl.minemod.cosmetics;

public enum CosmeticType {
    CAPE("Peleryny"),
    BACKPACK("Plecaki"),
    HAT("Czapki"),
    WINGS("Skrzydla"),
    PET("Zwierzaki");

    public final String label;

    CosmeticType(String label) { this.label = label; }
}
