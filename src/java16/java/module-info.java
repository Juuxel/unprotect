/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/**
 * Provides Unprotect, a ModLauncher plugin and Java agent.
 *
 * <p>Unprotect has three runtime implementations:
 * <ul>
 *     <li>{@link juuxel.unprotect.UnprotectLaunchPlugin} as a ModLauncher
 *     {@linkplain cpw.mods.modlauncher.serviceapi.ILaunchPluginService launch plugin service}
 *     <li>{@link juuxel.unprotect.UnprotectAgent} as an agent class
 *     <li>{@link juuxel.unprotect.UnprotectClassProcessor} as a FancyModLoader class processor
 * </ul>
 */
module io.github.juuxel.unprotect {
    requires org.objectweb.asm.tree;
    requires org.apache.logging.log4j;
    requires static org.jetbrains.annotations;

    // For the ModLauncher launch plugin service.
    requires static cpw.mods.modlauncher;
    requires static fml_loader;

    // For the instrumentation feature.
    requires static java.instrument;

    provides cpw.mods.modlauncher.serviceapi.ILaunchPluginService with juuxel.unprotect.UnprotectLaunchPlugin;
}
