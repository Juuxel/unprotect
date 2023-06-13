/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class TargetCache {
    private static final String TINY_2_0_PREFIX = "tiny\t2\t0\t";
    private static final String CLASS_PREFIX = "c\t";
    private final @Nullable Set<String> minecraftClasses = loadClasses();

    TargetCache() {
        if (minecraftClasses == null) {
            Transformation.LOGGER.warn("Could not load mappings from classpath, falling back to checking packages");
        } else {
            Transformation.LOGGER.info("Found {} Minecraft classes", minecraftClasses.size());
        }
    }

    private @Nullable Set<String> loadClasses() {
        try (@Nullable CloseableContainer<InputStream> in = getMappingStream()) {
            if (in == null) {
                Transformation.LOGGER.warn(
                    "Mappings not available! (mappings/mappings.tiny in {})",
                    System.getProperty(Transformation.MAPPING_LOCATION_SYSTEM_PROPERTY, "classpath")
                );
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in.value))) {
                return loadClasses(reader);
            }
        } catch (IOException e) {
            Transformation.LOGGER.error("Could not load mappings", e);
            return null;
        }
    }

    private @Nullable CloseableContainer<InputStream> getMappingStream() throws IOException {
        String pathString = System.getProperty(Transformation.MAPPING_LOCATION_SYSTEM_PROPERTY);

        if (pathString != null) {
            for (String path : pathString.split(Pattern.quote(File.pathSeparator))) {
                ZipFile zip = new ZipFile(new File(path));
                ZipEntry entry = zip.getEntry("mappings/mappings.tiny");
                if (entry == null) continue;
                return CloseableContainer.of(zip.getInputStream(entry), zip);
            }

            return null;
        } else {
            return CloseableContainer.of(TargetCache.class.getResourceAsStream("/mappings/mappings.tiny"));
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
                Transformation.LOGGER.warn("Unknown mapping format, should be Tiny v2");
                return null;
            }

            first = first.substring(TINY_2_0_PREFIX.length());
            List<String> namespaces = Arrays.asList(first.split("\t"));
            namespaceIndex = namespaces.indexOf("named");

            if (namespaceIndex == -1) {
                Transformation.LOGGER.warn("Could not find namespace 'named' in mappings (available: {})", namespaces);
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
