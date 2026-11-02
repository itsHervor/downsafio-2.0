package com.downsafio.menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Pantalla 3: lista de momentos (tarjetas con estado, titulo y descripcion). Clic en una tarjeta = temporizador. */
public class MomentosScreen extends Screen {
    private static final int ANCHO = 300;
    private final int idx;
    private int scroll = 0;

    public MomentosScreen(int idx) {
        super(Text.literal("Momentos"));
        this.idx = idx;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), b ->
                MinecraftClient.getInstance().setScreen(new ColorScreen(this.idx))).dimensions(8, 6, 22, 20).build());
    }

    private List<String> envolver(String texto, int ancho) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (this.textRenderer.getWidth(prueba) > ancho && actual.length() > 0) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) {
            lineas.add(actual.toString());
        }
        return lineas;
    }

    private int altoTarjeta(Cfg.Momento m) {
        return 34 + envolver(m.descripcion == null ? "" : m.descripcion, ANCHO - 16).size() * 10;
    }

    private int altoTotal() {
        int total = 0;
        for (Cfg.Momento m : Cfg.get().colores.get(this.idx).momentos) {
            total += altoTarjeta(m) + 8;
        }
        return total;
    }

    private void limitarScroll() {
        int max = Math.max(0, altoTotal() - (this.height - 50));
        this.scroll = Math.max(0, Math.min(this.scroll, max));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xF00B0B12);
        limitarScroll();
        int x = this.width / 2 - ANCHO / 2;
        int y = 38 - this.scroll;
        List<Cfg.Momento> lista = Cfg.get().colores.get(this.idx).momentos;
        if (lista.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No hay momentos para este color."), this.width / 2, 70, 0xFFAAAAAA);
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Agr\u00e9galos en config/downsafio_menu.json"), this.width / 2, 84, 0xFF777777);
        }
        for (Cfg.Momento m : lista) {
            int h = altoTarjeta(m);
            if (y + h > 30 && y < this.height) {
                boolean activo = Cmd.ACTIVOS.containsKey(this.idx + ":" + m.id);
                int borde = activo ? 0xFF2ECC71 : 0xFFC0283A;
                boolean encima = mouseX >= x && mouseX <= x + ANCHO && mouseY >= y && mouseY <= y + h && mouseY > 30;
                ctx.fill(x, y, x + ANCHO, y + h, encima ? 0xFF3A1216 : 0xFF2A0D10);
                ctx.fill(x, y, x + ANCHO, y + 1, borde);
                ctx.fill(x, y + h - 1, x + ANCHO, y + h, borde);
                ctx.fill(x, y, x + 1, y + h, borde);
                ctx.fill(x + ANCHO - 1, y, x + ANCHO, y + h, borde);
                ctx.drawTextWithShadow(this.textRenderer, Text.literal("\u25a0 " + (activo ? "ACTIVADO" : "DESACTIVADO")), x + 8, y + 6, borde);
                ctx.drawTextWithShadow(this.textRenderer, Text.literal(m.titulo == null ? m.id : m.titulo), x + 8, y + 18, 0xFFFFD34D);
                int ly = y + 30;
                for (String linea : envolver(m.descripcion == null ? "" : m.descripcion, ANCHO - 16)) {
                    ctx.drawTextWithShadow(this.textRenderer, Text.literal(linea), x + 8, ly, 0xFFDDDDDD);
                    ly += 10;
                }
            }
            y += h + 8;
        }
        // barra superior que tapa lo que se sale al desplazar
        ctx.fill(0, 0, this.width, 32, 0xFF0B0B12);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(Cfg.get().colores.get(this.idx).nombreMenu + " \u2014 MOMENTOS"), this.width / 2, 12, Cfg.COLORES[this.idx]);
        Cmd.marcaVersion(ctx, this.textRenderer, this.width, this.height);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseY > 32) {
            int x = this.width / 2 - ANCHO / 2;
            int y = 38 - this.scroll;
            for (Cfg.Momento m : Cfg.get().colores.get(this.idx).momentos) {
                int h = altoTarjeta(m);
                if (mouseX >= x && mouseX <= x + ANCHO && mouseY >= y && mouseY <= y + h) {
                    MinecraftClient.getInstance().setScreen(new TimerScreen(this.idx, m));
                    return true;
                }
                y += h + 8;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // sin @Override a proposito: si la firma cambiara, el menu seguiria funcionando con las flechas del teclado
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scroll -= (int) (verticalAmount * 24);
        limitarScroll();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            this.scroll += 24;
        } else if (keyCode == GLFW.GLFW_KEY_UP) {
            this.scroll -= 24;
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.scroll += 120;
        } else if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            this.scroll -= 120;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        limitarScroll();
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
