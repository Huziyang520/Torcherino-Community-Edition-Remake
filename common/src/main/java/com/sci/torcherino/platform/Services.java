package com.sci.torcherino.platform;

import com.sci.torcherino.Constants;
import com.sci.torcherino.platform.services.IPlatformHelper;
import com.sci.torcherino.platform.services.IRegistrationHelper;

import java.util.ServiceLoader;

/**
 * Service loader based access to the loader specific implementations. See the
 * MultiLoader template documentation for the general idea.
 */
public final class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static final IRegistrationHelper REGISTRATION = load(IRegistrationHelper.class);

    private Services() {
    }

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
