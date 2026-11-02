package com.downsafio.menu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Registra la tecla que abre el menu de la ruleta (por defecto: K, se puede cambiar en Controles). */
public class DownsafioMenuClient implements ClientModInitializer {
    public static KeyBinding abrir;

    @Override
    public void onInitializeClient() {
        abrir = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.downsafio_menu.abrir", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.downsafio_menu"));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("menuruleta").executes(ctx -> {
                    MinecraftClient mc = MinecraftClient.getInstance();
                    mc.execute(() -> mc.setScreen(new RuletaMenuScreen()));
                    return 1;
                })));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Cmd.tick(client);
            while (abrir.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new RuletaMenuScreen());
                }
            }
        });
    }
}
