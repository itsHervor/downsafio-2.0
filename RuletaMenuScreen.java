package com.downsafio.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** Pantalla 1: "Elige una ruleta". Rueda grande; al hacer clic en un color se abre el menu de ese color. */
public class RuletaMenuScreen extends Screen {
    private static final Identifier[] RUEDA = new Identifier[9];

    static {
        RUEDA[8] = Identifier.of("downsafio_menu", "textures/gui/rueda.png");
        for (int i = 0; i < 8; i++) {
            RUEDA[i] = Identifier.of("downsafio_menu", "textures/gui/rueda_" + i + ".png");
        }
    }

    private int cx;
    private int cy;
    private int d;

    public RuletaMenuScreen() {
        super(Text.literal("Elige una ruleta"));
    }

    @Override
    protected void init() {
        this.d = Math.max(120, Math.min(this.height - 100, 340));
        this.cx = this.width / 2;
        this.cy = this.height / 2 - 8;
        int by = this.height - 30;
        int bx = this.width / 2 - 209;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("ALEATORIA"), b -> {
            Cmd.enviar("ruleta random");
            this.close();
        }).dimensions(bx, by, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("DETENER RULETA"), b -> Cmd.enviar("ruleta stop"))
                .dimensions(bx + 106, by, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("NITIDEZ AUTO"), b -> Cmd.nitidezAuto())
                .dimensions(bx + 212, by, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("CERRAR"), b -> this.close())
                .dimensions(bx + 318, by, 100, 20).build());
    }

    private int sectorEn(double mx, double my) {
        double dx = mx - this.cx;
        double dy = my - this.cy;
        double dist = Math.hypot(dx, dy);
        if (dist > this.d / 2.0 * 0.97 || dist < 8) {
            return -1;
        }
        double ang = Math.toDegrees(Math.atan2(dx, -dy));
        if (ang < 0) {
            ang += 360;
        }
        return ((int) (ang / 45)) % 8;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xC0000000);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Elige una ruleta"), this.width / 2, 14, 0xFFFFFFFF);
        int h = sectorEn(mouseX, mouseY);
        RenderSystem.enableBlend();
        ctx.drawTexture(RUEDA[h >= 0 ? h : 8], this.cx - this.d / 2, this.cy - this.d / 2, this.d, this.d, 0f, 0f, 512, 512, 512, 512);
        String nombre = h >= 0 ? Cfg.get().colores.get(h).nombreMenu : "(haz clic en un color)";
        int col = h >= 0 ? Cfg.COLORES[h] : 0xFFAAAAAA;
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(nombre), this.width / 2, this.cy + this.d / 2 + 8, col);
        Cmd.marcaVersion(ctx, this.textRenderer, this.width, this.height);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int s = sectorEn(mouseX, mouseY);
            if (s >= 0) {
                MinecraftClient.getInstance().setScreen(new ColorScreen(s));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
