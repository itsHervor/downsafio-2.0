package com.downsafio.menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Pantalla 4: ventana de temporizador para activar (o desactivar) un momento. */
public class TimerScreen extends Screen {
    private final int idx;
    private final Cfg.Momento momento;
    private TextFieldWidget tiempo;

    public TimerScreen(int idx, Cfg.Momento momento) {
        super(Text.literal("Temporizador"));
        this.idx = idx;
        this.momento = momento;
    }

    private String clave() {
        return this.idx + ":" + this.momento.id;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        boolean activo = Cmd.ACTIVOS.containsKey(clave());
        if (!activo) {
            this.tiempo = new TextFieldWidget(this.textRenderer, cx - 70, cy - 8, 140, 20, Text.literal("Tiempo"));
            this.tiempo.setMaxLength(12);
            this.tiempo.setText("5m");
            this.addDrawableChild(this.tiempo);
        }
        this.addDrawableChild(ButtonWidget.builder(Text.literal(activo ? "DESACTIVAR" : "ACTIVAR"), b -> {
            if (activo) {
                desactivar();
            } else {
                activar();
            }
        }).dimensions(cx - 102, cy + 20, 100, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("CANCELAR"), b ->
                MinecraftClient.getInstance().setScreen(new MomentosScreen(this.idx))).dimensions(cx + 2, cy + 20, 100, 20).build());
    }

    private void activar() {
        int seg = Cmd.parseSegundos(this.tiempo.getText());
        if (seg <= 0) {
            Cmd.aviso("Tiempo no v\u00e1lido. Ejemplos: 30s, 5m, 1h30m");
            return;
        }
        for (String c : this.momento.alActivar) {
            Cmd.enviar(c);
        }
        if (this.momento.anuncio != null && !this.momento.anuncio.isEmpty()) {
            Cmd.enviar("title @a times 10 100 20");
            Cmd.enviar("title @a subtitle " + Cmd.json(this.momento.anuncio, "white", false));
            Cmd.enviar("title @a title " + Cmd.json(this.momento.titulo == null ? this.momento.id : this.momento.titulo, "gold", true));
        }
        final int i = this.idx;
        final Cfg.Momento m = this.momento;
        Cmd.Tarea t = Cmd.despues(seg, () -> terminar(i, m));
        if (t != null) {
            Cmd.ACTIVOS.put(clave(), t);
        }
        MinecraftClient.getInstance().setScreen(new MomentosScreen(this.idx));
    }

    private void desactivar() {
        Cmd.Tarea t = Cmd.ACTIVOS.get(clave());
        if (t != null) {
            t.cancelada = true;
        }
        terminar(this.idx, this.momento);
        MinecraftClient.getInstance().setScreen(new MomentosScreen(this.idx));
    }

    static void terminar(int idx, Cfg.Momento m) {
        for (String c : m.alTerminar) {
            Cmd.enviar(c);
        }
        Cmd.ACTIVOS.remove(idx + ":" + m.id);
        Cmd.enviar("tellraw @a " + Cmd.json("Termin\u00f3: " + (m.titulo == null ? m.id : m.titulo), "gray", false));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0xD8000000);
        int cx = this.width / 2;
        int cy = this.height / 2;
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(this.momento.titulo == null ? this.momento.id : this.momento.titulo), cx, cy - 56, 0xFFFFAA00);
        boolean activo = Cmd.ACTIVOS.containsKey(clave());
        if (activo) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Este momento est\u00e1 ACTIVADO."), cx, cy - 34, 0xFF2ECC71);
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Pulsa DESACTIVAR para terminarlo ahora."), cx, cy - 18, 0xFFBBBBBB);
        } else {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("TEMPORIZADOR: cu\u00e1nto dura el momento"), cx, cy - 34, 0xFFFFFFFF);
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Ejemplos: 30s, 5m, 1h30m"), cx, cy + 46, 0xFF888888);
        }
        Cmd.marcaVersion(ctx, this.textRenderer, this.width, this.height);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
