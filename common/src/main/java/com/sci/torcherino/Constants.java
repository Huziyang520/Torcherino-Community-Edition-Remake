package com.sci.torcherino;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod-wide constants. Kept package-private in scope on purpose: the values mirror
 * the original Torcherino 7.5 metadata so that data packs, addons and existing
 * world saves keep resolving the same identifiers.
 */
public final class Constants {

    /** Original modId, kept verbatim. */
    public static final String MOD_ID = "torcherino";

    /** Original display name, kept verbatim (lower-case in 7.5). */
    public static final String MOD_NAME = "torcherino";

    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private Constants() {
    }
}
