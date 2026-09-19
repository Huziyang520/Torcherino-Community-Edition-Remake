/*
 * 本文件：模组级常量。
 * 说明：集中存放 modId、显示名与统一日志器。
 */
package com.sci.torcherino;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod-wide constants. The modId is baked into every saved identifier, so data packs,
 * addons and existing world saves keep resolving the same names.
 */
public final class Constants {

    /** Original modId, kept verbatim. */
    public static final String MOD_ID = "torcherino";

    /** Resource namespace and logger name. */
    public static final String MOD_NAME = "torcherino";

    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private Constants() {
    }
}
