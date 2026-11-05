/*
 * 本文件：把编辑界面交给客户端显示。
 * 说明：各版本「打开界面」的入口不同（26.1.2 只有 Minecraft#setScreen，26.2 起改为 Gui#setScreen），
 *      直接引用其一会在另一版本上抛 NoSuchMethodError；setScreenAndShow 三版都有，但会额外强制渲染一帧，
 *      右键时遮罩会闪一下，因此只作兜底。
 */
package com.sci.torcherino.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Method;

/**
 * Shows a screen through the cheapest entry point the running version provides.
 *
 * <p>The call is always queued on the game thread: the packet handlers run before the tick
 * is finished, and a screen switched mid tick is composited differently for one frame.</p>
 */
public final class TorcherinoScreenOpener {

    /** {@code Gui#setScreen} (26.2+) or {@code Minecraft#setScreen} (26.1.2). */
    private static Method setScreen;
    private static boolean resolved;

    private TorcherinoScreenOpener() {
    }

    public static void open(Screen screen) {
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> show(minecraft, screen));
    }

    private static void show(Minecraft minecraft, Screen screen) {
        final Method method = resolve();
        if (method != null) {
            try {
                final Object target = method.getDeclaringClass() == Gui.class ? minecraft.gui : minecraft;
                method.invoke(target, screen);
                return;
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Fall through: the forced frame below still shows the screen.
            }
        }
        minecraft.setScreenAndShow(screen);
    }

    private static Method resolve() {
        if (resolved) {
            return setScreen;
        }
        resolved = true;
        setScreen = find(Gui.class, "setScreen");
        if (setScreen == null) {
            setScreen = find(Minecraft.class, "setScreen");
        }
        return setScreen;
    }

    private static Method find(Class<?> owner, String name) {
        try {
            return owner.getMethod(name, Screen.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
