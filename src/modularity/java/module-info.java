/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

module io.github.juuxel.unprotect {
    requires org.objectweb.asm.tree;
    requires cpw.mods.modlauncher;
    requires static org.jetbrains.annotations;

    provides cpw.mods.modlauncher.serviceapi.ILaunchPluginService with juuxel.unprotect.UnprotectLaunchPlugin;
}
