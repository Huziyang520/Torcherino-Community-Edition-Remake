/*
 * 本文件：Forge 侧 Jade 插件入口。
 * 说明：Jade 只在客户端扫描带 @WailaPlugin 注解的类，因此这个类也只会被 Jade 加载；
 *      未安装 Jade 时它不会被任何人引用，模组照常运行。
 */
package com.sci.torcherino.compat.jade;

import com.sci.torcherino.Constants;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Forge entry point of the optional Jade integration. The annotation value is the plugin id,
 * which is also the category name Jade uses in its plugin configuration.
 */
@WailaPlugin(Constants.MOD_ID)
public class TorcherinoJadeForgePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        TorcherinoJadeProvider.register(registration);
    }
}
