package pl.minemod.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import pl.minemod.cosmetics.Cosmetic;
import pl.minemod.cosmetics.CosmeticRegistry;
import pl.minemod.cosmetics.CosmeticType;
import pl.minemod.modules.MineModule;
import pl.minemod.modules.ModuleManager;

import java.util.ArrayList;
import java.util.List;

/** Menu MineMod w stylu Lunar Client: karty w siatce, szaro-bialy motyw. */
public class MineModScreen extends Screen {
    private static final int PANEL_W = 440;
    private static final int PANEL_H = 254;
    private static final int PER_PAGE = 6;

    private static final int C_DIM = 0xB0000000;
    private static final int C_PANEL = 0xF0181818;
    private static final int C_BAR = 0xF0101010;
    private static final int C_LINE = 0xFF333333;
    private static final int C_CARD = 0xFF222222;
    private static final int C_CARD_BORDER = 0xFF3A3A3A;
    private static final int C_ICON = 0xFF2E2E2E;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF9A9A9A;

    private enum Tab { MODS, COSMETICS }

    private record Visual(int x, int y, int w, int h, String label, boolean filled) {}
    private record Card(int x, int y, String tag, String name, String sub) {}

    private final List<Visual> visuals = new ArrayList<>();
    private final List<Card> cards = new ArrayList<>();

    private Tab tab = Tab.MODS;
    private int page = 0;
    private MineModule.Category modFilter = null;
    private CosmeticType cosFilter = null;
    private String pagerText = "";
    private int pagerX = 0, pagerY = 0;

    public MineModScreen() {
        super(Text.literal("MineMod"));
    }

    private int left() { return (width - PANEL_W) / 2; }
    private int top() { return (height - PANEL_H) / 2; }

    /** Niewidzialny przycisk (obsluguje klikniecie) + wlasny wyglad rysowany w render(). */
    private void btn(int x, int y, int w, int h, String label, boolean filled, Runnable action) {
        visuals.add(new Visual(x, y, w, h, label, filled));
        ButtonWidget b = ButtonWidget.builder(Text.literal(label), bw -> action.run())
                .dimensions(x, y, w, h).build();
        b.setAlpha(0f);
        addDrawableChild(b);
    }

    @Override
    protected void init() {
        clearChildren();
        visuals.clear();
        cards.clear();
        pagerText = "";

        int x = left(), y = top();

        btn(x + 150, y + 5, 80, 14, "MODS", tab == Tab.MODS, () -> { tab = Tab.MODS; page = 0; init(); });
        btn(x + 236, y + 5, 80, 14, "COSMETICS", tab == Tab.COSMETICS, () -> { tab = Tab.COSMETICS; page = 0; init(); });
        btn(x + PANEL_W - 22, y + 5, 16, 14, "X", false, this::close);

        int total;
        if (tab == Tab.MODS) {
            btn(x + 17, y + 30, 56, 14, "ALL", modFilter == null, () -> { modFilter = null; page = 0; init(); });
            int i = 1;
            for (MineModule.Category cat : MineModule.Category.values()) {
                btn(x + 17 + i * 60, y + 30, 56, 14, cat.label, modFilter == cat, () -> { modFilter = cat; page = 0; init(); });
                i++;
            }
            List<MineModule> mods = ModuleManager.ALL.stream()
                    .filter(m -> modFilter == null || m.category == modFilter).toList();
            total = mods.size();
            clampPage(total);
            int start = page * PER_PAGE, end = Math.min(total, start + PER_PAGE);
            for (int k = start; k < end; k++) {
                MineModule m = mods.get(k);
                int cx = x + 17 + ((k - start) % 3) * 138;
                int cy = y + 52 + ((k - start) / 3) * 88;
                cards.add(new Card(cx, cy, m.tag, m.name, ""));
                btn(cx + 6, cy + 50, 118, 13, "OPTIONS: " + m.optionLabel(), false, () -> { m.cycleOption(); init(); });
                btn(cx + 6, cy + 65, 118, 13, m.isEnabled() ? "ENABLED" : "DISABLED", m.isEnabled(), () -> { m.toggle(); init(); });
            }
        } else {
            btn(x + 17, y + 30, 50, 14, "ALL", cosFilter == null, () -> { cosFilter = null; page = 0; init(); });
            int i = 1;
            for (CosmeticType t : CosmeticType.values()) {
                btn(x + 17 + i * 54, y + 30, 50, 14, t.label.toUpperCase(), cosFilter == t, () -> { cosFilter = t; page = 0; init(); });
                i++;
            }
            List<Cosmetic> list = CosmeticRegistry.all().stream()
                    .filter(c -> cosFilter == null || c.type() == cosFilter).toList();
            total = list.size();
            clampPage(total);
            int start = page * PER_PAGE, end = Math.min(total, start + PER_PAGE);
            for (int k = start; k < end; k++) {
                Cosmetic c = list.get(k);
                int cx = x + 17 + ((k - start) % 3) * 138;
                int cy = y + 52 + ((k - start) / 3) * 88;
                String tag = c.type().name().substring(0, Math.min(4, c.type().name().length()));
                cards.add(new Card(cx, cy, tag, c.name().toUpperCase(), c.type().label.toUpperCase()));
                boolean on = CosmeticRegistry.isEquipped(c);
                btn(cx + 6, cy + 65, 118, 13, on ? "EQUIPPED" : "EQUIP", on, () -> { CosmeticRegistry.toggle(c); init(); });
            }
        }

        int maxPage = Math.max(0, (total - 1) / PER_PAGE);
        if (maxPage > 0) {
            btn(x + PANEL_W / 2 - 60, y + 232, 20, 14, "<", false, () -> { if (page > 0) page--; init(); });
            btn(x + PANEL_W / 2 + 40, y + 232, 20, 14, ">", false, () -> { if (page < maxPage) page++; init(); });
            pagerText = (page + 1) + " / " + (maxPage + 1);
            pagerX = x + PANEL_W / 2;
            pagerY = y + 236;
        }
    }

    private void clampPage(int total) {
        int maxPage = Math.max(0, (total - 1) / PER_PAGE);
        if (page > maxPage) page = maxPage;
        if (page < 0) page = 0;
    }

    private void rect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + h, color);
    }

    private void border(DrawContext ctx, int x, int y, int w, int h, int color) {
        rect(ctx, x, y, w, 1, color);
        rect(ctx, x, y + h - 1, w, 1, color);
        rect(ctx, x, y, 1, h, color);
        rect(ctx, x + w - 1, y, 1, h, color);
    }

    private void centered(DrawContext ctx, String s, int cx, int y, int color) {
        ctx.drawTextWithShadow(textRenderer, s, cx - textRenderer.getWidth(s) / 2, y, color);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, C_DIM);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        int x = left(), y = top();
        rect(ctx, x, y, PANEL_W, PANEL_H, C_PANEL);
        rect(ctx, x, y, PANEL_W, 24, C_BAR);
        rect(ctx, x, y + 24, PANEL_W, 1, C_LINE);
        border(ctx, x, y, PANEL_W, PANEL_H, C_LINE);

        ctx.drawTextWithShadow(textRenderer, "MINEMOD", x + 10, y + 9, WHITE);

        for (Card c : cards) {
            rect(ctx, c.x(), c.y(), 130, 80, C_CARD);
            border(ctx, c.x(), c.y(), 130, 80, C_CARD_BORDER);
            rect(ctx, c.x() + 45, c.y() + 5, 40, 28, C_ICON);
            centered(ctx, c.tag(), c.x() + 65, c.y() + 15, GRAY);
            centered(ctx, c.name(), c.x() + 65, c.y() + 37, WHITE);
            if (!c.sub().isEmpty()) centered(ctx, c.sub(), c.x() + 65, c.y() + 50, GRAY);
        }

        for (Visual v : visuals) {
            boolean hover = mouseX >= v.x() && mouseX < v.x() + v.w() && mouseY >= v.y() && mouseY < v.y() + v.h();
            int bg, fg;
            if (v.filled()) {
                bg = hover ? 0xFFE6E6E6 : 0xFFFFFFFF;
                fg = 0xFF000000;
            } else {
                bg = hover ? 0xFF404040 : 0xFF2E2E2E;
                fg = 0xFFD0D0D0;
            }
            rect(ctx, v.x(), v.y(), v.w(), v.h(), bg);
            if (!v.filled()) border(ctx, v.x(), v.y(), v.w(), v.h(), 0xFF444444);
            centered(ctx, v.label(), v.x() + v.w() / 2, v.y() + (v.h() - 8) / 2 + 1, fg);
        }

        if (!pagerText.isEmpty()) centered(ctx, pagerText, pagerX, pagerY, GRAY);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
