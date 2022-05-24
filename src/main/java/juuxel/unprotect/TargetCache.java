/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class TargetCache {
    private static final String TINY_2_0_PREFIX = "tiny\t2\t0\t";
    private static final String CLASS_PREFIX = "c\t";
    private final @Nullable Set<String> minecraftClasses = loadClasses();

    TargetCache() {
        if (minecraftClasses == null) {
            UnprotectLaunchPlugin.LOGGER.warn("Could not load mappings from classpath, falling back to checking packages");
        } else {
            UnprotectLaunchPlugin.LOGGER.info("Found {} Minecraft classes", minecraftClasses.size());
        }
    }

    private @Nullable Set<String> loadClasses() {
        try (InputStream in = TargetCache.class.getResourceAsStream("/mappings/mappings.tiny")) {
            if (in == null) {
                UnprotectLaunchPlugin.LOGGER.warn("Mappings not available on classpath at /mappings/mappings.tiny");
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return loadClasses(reader);
            }
        } catch (IOException e) {
            UnprotectLaunchPlugin.LOGGER.error("Could not load mappings", e);
            return null;
        }
    }

    private @Nullable Set<String> loadClasses(BufferedReader reader) throws IOException {
        Set<String> classes = new HashSet<>();
        int namespaceIndex;

        // Find index of namespace 'named'
        {
            String first = reader.readLine();

            // Unknown mapping format
            if (!first.startsWith(TINY_2_0_PREFIX)) {
                UnprotectLaunchPlugin.LOGGER.warn("Unknown mapping format, should be Tiny v2");
                return null;
            }

            first = first.substring(TINY_2_0_PREFIX.length());
            List<String> namespaces = Arrays.asList(first.split("\t"));
            namespaceIndex = namespaces.indexOf("named");

            if (namespaceIndex == -1) {
                UnprotectLaunchPlugin.LOGGER.warn("Could not find namespace 'named' in mappings (available: {})", namespaces);
                return null;
            }
        }

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(CLASS_PREFIX)) {
                line = line.substring(CLASS_PREFIX.length());
                String[] names = line.split("\t");
                String name = names[namespaceIndex];

                if (name.isEmpty()) {
                    name = names[0];
                }

                classes.add(name);
            }
        }

        return classes;
    }

    boolean isMinecraftClass(Type type) {
        if (minecraftClasses != null) {
            return minecraftClasses.contains(type.getInternalName());
        } else {
            String internalName = type.getInternalName();
            return internalName.startsWith(Packages.MINECRAFT) || internalName.startsWith(Packages.MOJANG);
        }
    }
}
