package pl.minemod.modules;

import java.util.List;

public final class ModuleManager {
    private static final String[] BG = {"TLO: TAK", "TLO: NIE"};

    public static final MineModule FPS = new MineModule("fps", "FPS", "FPS", MineModule.Category.HUD, true, BG);
    public static final MineModule CPS = new MineModule("cps", "CPS", "CPS", MineModule.Category.HUD, true, BG);
    public static final MineModule PING = new MineModule("ping", "PING", "MS", MineModule.Category.HUD, true, BG);
    public static final MineModule SPRINT = new MineModule("sprint", "SPRINT STATUS", "RUN", MineModule.Category.HUD, true, BG);
    public static final MineModule COORDS = new MineModule("coords", "KOORDYNATY", "XYZ", MineModule.Category.HUD, false, BG);
    public static final MineModule CROSSHAIR = new MineModule("crosshair", "CELOWNIK", "+", MineModule.Category.VISUAL, false,
            "KRZYZ", "KROPKA", "PLUS", "OKRAG");

    public static final List<MineModule> ALL = List.of(FPS, CPS, PING, SPRINT, COORDS, CROSSHAIR);

    private ModuleManager() {}
}
