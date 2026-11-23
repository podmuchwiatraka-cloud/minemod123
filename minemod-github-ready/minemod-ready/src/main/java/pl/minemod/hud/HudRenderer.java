package pl.minemod.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import pl.minemod.modules.MineModule;
import pl.minemod.modules.ModuleManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Rysuje FPS / CPS / ping / sprint / koordynaty oraz wlasny celownik. */
public final class HudRenderer {
    private static final Deque<Long> LEFT = new ArrayDeque<>();
    private static final Deque<Long> RIGHT = new ArrayDeque<>();
    private static boolean lastLeft, lastRight;

    private record Line(String text, boolean bg) {}

    private HudRenderer() {}

    public static void register() {
        HudElementRegistry.addLast(Identifier.of("minemod", "info"), (ctx, tick) -> renderInfo(ctx));

        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (ctx, tick) -> {
            if (ModuleManager.CROSSHAIR.isEnabled()) renderCrosshair(ctx);
            else original.render(ctx, tick);
        });
    }

    private static void pollClicks(MinecraftClient mc) {
        if (mc.currentScreen != null) {
            lastLeft = false;
            lastRight = false;
            return;
        }
        long handle = mc.getWindow().getHandle();
        boolean l = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();
        if (l && !lastLeft) LEFT.addLast(now);
        if (r && !lastRight) RIGHT.addLast(now);
        lastLeft = l;
        lastRight = r;
        while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.pollFirst();
        while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.pollFirst();
    }

    private static void renderInfo(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        pollClicks(mc);

        List<Line> lines = new ArrayList<>();

        if (ModuleManager.FPS.isEnabled()) {
            lines.add(new Line("FPS: " + mc.getCurrentFps(), ModuleManager.FPS.option() == 0));
        }
        if (ModuleManager.CPS.isEnabled()) {
            lines.add(new Line("CPS: " + LEFT.size() + " | " + RIGHT.size(), ModuleManager.CPS.option() == 0));
        }
        if (ModuleManager.PING.isEnabled()) {
            ClientPlayNetworkHandler nh = mc.getNetworkHandler();
            PlayerListEntry entry = nh == null ? null : nh.getPlayerListEntry(mc.player.getUuid());
            int ping = entry == null ? 0 : entry.getLatency();
            lines.add(new Line("PING: " + ping + " ms", ModuleManager.PING.option() == 0));
        }
        if (ModuleManager.SPRINT.isEnabled()) {
            String s = mc.player.isSprinting() ? "SPRINTING" : (mc.player.isSneaking() ? "SNEAKING" : "WALKING");
            lines.add(new Line("[" + s + "]", ModuleManager.SPRINT.option() == 0));
        }
        if (ModuleManager.COORDS.isEnabled()) {
            lines.add(new Line("XYZ: " + mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ(),
                    ModuleManager.COORDS.option() == 0));
        }

        TextRenderer tr = mc.textRenderer;
        int x = 6, y = 6;
        for (Line line : lines) {
            int w = tr.getWidth(line.text());
            if (line.bg()) ctx.fill(x - 3, y - 3, x + w + 3, y + 11, 0x90000000);
            ctx.drawTextWithShadow(tr, line.text(), x, y, 0xFFFFFFFF);
            y += 17;
        }
    }

    private static void renderCrosshair(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!mc.options.getPerspective().isFirstPerson()) return;

        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;
        int white = 0xFFFFFFFF;
        int shade = 0xFF000000;

        switch (ModuleManager.CROSSHAIR.option()) {
            case 1 -> { // kropka
                ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, shade);
                ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, white);
            }
            case 2 -> { // gruby plus
                ctx.fill(cx - 6, cy - 2, cx + 6, cy + 2, shade);
                ctx.fill(cx - 2, cy - 6, cx + 2, cy + 6, shade);
                ctx.fill(cx - 5, cy - 1, cx + 5, cy + 1, white);
                ctx.fill(cx - 1, cy - 5, cx + 1, cy + 5, white);
            }
            case 3 -> { // okrag (kwadratowa ramka)
                ctx.fill(cx - 5, cy - 5, cx + 5, cy + 5, shade);
                ctx.fill(cx - 4, cy - 4, cx + 4, cy + 4, white);
                ctx.fill(cx - 3, cy - 3, cx + 3, cy + 3, shade);
                ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0x00000000);
            }
            default -> { // krzyz z przerwa
                ctx.fill(cx - 7, cy - 1, cx - 2, cy + 1, white);
                ctx.fill(cx + 2, cy - 1, cx + 7, cy + 1, white);
                ctx.fill(cx - 1, cy - 7, cx + 1, cy - 2, white);
                ctx.fill(cx - 1, cy + 2, cx + 1, cy + 7, white);
            }
        }
    }
}
