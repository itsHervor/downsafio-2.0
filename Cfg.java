package com.downsafio.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Configuracion editable en config/downsafio_menu.json (nombres de menu y lista de momentos por color). */
public class Cfg {
    public static final String[] NOMBRES = {"ROJO OSCURO", "NARANJA", "AMARILLO", "VERDE AQUA", "AZUL CIELO", "AZUL MARINO", "MORADO CLARO", "ROSA PASTEL"};
    public static final String[] CMDS = {"roja", "naranja", "amarilla", "verde", "celeste", "azul", "morada", "rosa"};
    public static final int[] COLORES = {0xFFC0283A, 0xFFFF8A1F, 0xFFFFD60A, 0xFF1FE0B0, 0xFF5BC8FF, 0xFF3F63D6, 0xFFB79CFF, 0xFFFFB7D5};

    public double retrasoAnuncio = 15.0;
    public List<ColorCfg> colores = new ArrayList<>();

    public static class ColorCfg {
        public String nombreMenu;
        public double escala = 1.0;
        public List<Momento> momentos = new ArrayList<>();
    }

    public static class Momento {
        public String id;
        public String titulo;
        public String descripcion;
        public String anuncio;
        public List<String> alActivar = new ArrayList<>();
        public List<String> alTerminar = new ArrayList<>();
    }

    private static Cfg instancia;

    public static Cfg get() {
        if (instancia == null) {
            instancia = cargar();
        }
        return instancia;
    }

    private static Path ruta() {
        return FabricLoader.getInstance().getConfigDir().resolve("downsafio_menu.json");
    }

    private static Cfg cargar() {
        try {
            Path r = ruta();
            if (Files.exists(r)) {
                try (Reader rd = Files.newBufferedReader(r)) {
                    Cfg c = new Gson().fromJson(rd, Cfg.class);
                    if (c != null) {
                        c.normalizar();
                        return c;
                    }
                }
            }
        } catch (Exception ignorada) {
            // si el archivo esta danado se vuelve a crear con los valores por defecto
        }
        Cfg c = porDefecto();
        c.guardar();
        return c;
    }

    public void guardar() {
        try {
            Path r = ruta();
            Files.createDirectories(r.getParent());
            try (Writer w = Files.newBufferedWriter(r)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(this, w);
            }
        } catch (Exception ignorada) {
            // no es critico
        }
    }

    private void normalizar() {
        while (colores.size() < 8) {
            ColorCfg c = new ColorCfg();
            c.nombreMenu = "MOMENTO " + NOMBRES[colores.size()];
            colores.add(c);
        }
        for (ColorCfg c : colores) {
            if (c.momentos == null) {
                c.momentos = new ArrayList<>();
            }
            if (c.nombreMenu == null) {
                c.nombreMenu = "MOMENTO";
            }
            for (Momento m : c.momentos) {
                if (m.alActivar == null) {
                    m.alActivar = new ArrayList<>();
                }
                if (m.alTerminar == null) {
                    m.alTerminar = new ArrayList<>();
                }
                if (m.id == null) {
                    m.id = String.valueOf(m.hashCode());
                }
            }
        }
    }

    private static Cfg porDefecto() {
        Cfg cfg = new Cfg();
        for (int i = 0; i < 8; i++) {
            ColorCfg c = new ColorCfg();
            c.nombreMenu = i == 0 ? "MOMENTO DOWN" : "MOMENTO " + NOMBRES[i];
            cfg.colores.add(c);
        }
        // Ejemplos editables (puedes cambiarlos o borrarlos en config/downsafio_menu.json)
        Momento a = new Momento();
        a.id = "sin_regeneracion";
        a.titulo = "Momento Down \u2014 Sin regeneraci\u00f3n natural";
        a.descripcion = "Hasta que termine el contador nadie puede regenerar vida de forma natural.";
        a.anuncio = "Sin regeneraci\u00f3n natural hasta que termine el contador";
        a.alActivar.add("gamerule naturalRegeneration false");
        a.alTerminar.add("gamerule naturalRegeneration true");
        Momento b = new Momento();
        b.id = "ceguera";
        b.titulo = "Momento Down \u2014 Ceguera";
        b.descripcion = "Todos los jugadores quedan ciegos mientras dure el contador.";
        b.anuncio = "Ceguera para todos";
        b.alActivar.add("effect give @a minecraft:blindness infinite 0 true");
        b.alTerminar.add("effect clear @a minecraft:blindness");
        cfg.colores.get(0).momentos.add(a);
        cfg.colores.get(0).momentos.add(b);
        return cfg;
    }
}
