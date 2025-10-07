/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect.modlauncher;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import juuxel.unprotect.Transformation;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;

/**
 * Unprotect's launch plugin service that processes the necessary classes.
 * The transformations only run for non-empty classes in the
 * {@link cpw.mods.modlauncher.serviceapi.ILaunchPluginService.Phase#AFTER AFTER} phase.
 */
public final class UnprotectLaunchPlugin implements ILaunchPluginService {
    /**
     * A system property ({@value}) for determining which classes to transform.
     *
     * <p>Possible values (case-insensitive with respect to {@link java.util.Locale#ROOT}):
     * <ul>
     *     <li>{@code all}: apply to all classes</li>
     *     <li>{@code minecraft+forge} (default): apply to Minecraft and (Neo)Forge classes</li>
     *     <li>{@code none}: disable Unprotect completely and apply to no classes</li>
     * </ul>
     *
     * @since 1.1.0
     */
    public static final String TARGET_SYSTEM_PROPERTY = Transformation.TARGET_SYSTEM_PROPERTY;

    /**
     * A system property ({@value}) for determining where to load mappings for
     * the {@code minecraft+forge} {@linkplain #TARGET_SYSTEM_PROPERTY target}.
     *
     * <p>The value should be a file path to a zip containing the mapping files at {@code mappings/mappings.tiny}
     * in Tiny v2 format. It can also be a list of file paths separated by {@link java.io.File#pathSeparator},
     * of which the first suitable one will be used.
     *
     * <p>If absent, Unprotect will try to load them from the classpath at the same file path.
     *
     * @since 1.2.0
     */
    public static final String MAPPING_LOCATION_SYSTEM_PROPERTY = Transformation.MAPPING_LOCATION_SYSTEM_PROPERTY;

    private final Transformation transformation = new Transformation("ModLauncher");

    @Override
    public String name() {
        return "unprotect";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return transformation.handlesClass(classType, isEmpty) ? EnumSet.of(Phase.AFTER) : EnumSet.noneOf(Phase.class);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        return transformation.processClass(classNode);
    }
}
