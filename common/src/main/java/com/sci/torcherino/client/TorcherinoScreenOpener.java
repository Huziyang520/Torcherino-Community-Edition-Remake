/*
 * 本文件：把编辑界面交给客户端显示。
 * 说明：26.x 的 Minecraft#setScreenAndShow 会额外强制渲染一整帧（26.1.2 / 26.3 字节码均为 setScreen + renderFrame(false)），
 *      这一帧在 tick 中途执行，遮罩与面板的合成和随后的正常帧不一致，肉眼就是打开时闪一下。
 *      原版容器界面从不强制帧，只在客户端线程上把界面设进去，所以这里同样排队到客户端线程再设。
 *      各版本的入口不同（26.1.2 只有 Minecraft#setScreen，26.2 起改成 Gui#setScreen），按名解析，兜底才是 setScreenAndShow。
 */
package com.sci.torcherino.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Method;

/**
 * Shows a screen without forcing an extra frame.
 *
 * <p>The call is queued on the game thread: the packet handlers run before the tick is
 * finished, and a screen switched mid tick is composited differently for one frame.</p>
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
