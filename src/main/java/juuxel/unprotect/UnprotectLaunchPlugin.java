/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

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
     *     <li>{@code minecraft+forge} (default): apply to Minecraft and Forge classes</li>
     *     <li>{@code none}: disable Unprotect completely and apply to no classes</li>
     * </ul>
     *
     * @since 1.1.0
     */
    public static final String TARGET_SYSTEM_PROPERTY = "unprotect.target";

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
    public static final String MAPPING_LOCATION_SYSTEM_PROPERTY = "unprotect.mappings";

    static final Logger LOGGER = LogManager.getLogger("unprotect");

    // Package-private doesn't have its own access flag and is used when there's
    // none of these other flags.
    private static final int ACCESS_MASK = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE;

    private @Nullable Target target;
    private @Nullable TargetCache targetCache;

    @Override
    public String name() {
        return "unprotect";
    }

    private Target getTarget() {
        if (target == null) {
            String targetId = System.getProperty(TARGET_SYSTEM_PROPERTY, Target.MINECRAFT_AND_FORGE.id);
            target = Target.BY_ID.get(targetId);

            if (target == null) {
                LOGGER.error("Unknown Unprotect target: {} (available: {}), falling back to minecraft+forge", targetId, Target.BY_ID.keySet());
                target = Target.MINECRAFT_AND_FORGE;
            }
        }

        return target;
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        if (isEmpty) {
            return EnumSet.noneOf(Phase.class);
        }

        switch (getTarget()) {
            case ALL:
                return EnumSet.of(Phase.AFTER);
            case MINECRAFT_AND_FORGE:
                if (classType.getInternalName().startsWith(Packages.FORGE)) {
                    return EnumSet.of(Phase.AFTER);
                }

                if (targetCache == null) {
                    targetCache = new TargetCache();
                }

                return targetCache.isMinecraftClass(classType) ? EnumSet.of(Phase.AFTER) : EnumSet.noneOf(Phase.class);
            case NONE:
            default:
                return EnumSet.noneOf(Phase.class);
        }
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
        boolean changed; // whether the access of anything in this node has changed

        int original = classNode.access;
        classNode.access = changeAccess(classNode.access);
        changed = original != classNode.access;

        for (InnerClassNode innerClass : classNode.innerClasses) {
            original = innerClass.access;
            innerClass.access = changeAccess(innerClass.access);
            changed |= original != innerClass.access;
        }

        for (FieldNode field : classNode.fields) {
            original = field.access;
            field.access = changeAccess(field.access);
            changed |= original != field.access;
        }

        for (MethodNode method : classNode.methods) {
            original = method.access;
            method.access = changeAccess(method.access);
            changed |= original != method.access;
        }

        return changed;
    }

    @VisibleForTesting
    static int changeAccess(int access) {
        // Leave private members intact
        if ((access & Opcodes.ACC_PRIVATE) != 0) return access;

        return (access & ~ACCESS_MASK) | Opcodes.ACC_PUBLIC;
    }

    private enum Target {
        ALL("all"),
        MINECRAFT_AND_FORGE("minecraft+forge"),
        NONE("none");

        private static final Map<String, Target> BY_ID = new HashMap<>();
        private final String id;

        Target(String id) {
            this.id = id;
        }

        static {
            for (Target target : values()) {
                BY_ID.put(target.id, target);
            }
        }
    }
}
