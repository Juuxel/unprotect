/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect;

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

import java.util.HashMap;
import java.util.Map;

final class Transformation {
    // Package-private doesn't have its own access flag and is used when there's
    // none of these other flags.
    private static final int ACCESS_MASK = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE;
    static final String TARGET_SYSTEM_PROPERTY = "unprotect.target";
    static final String MAPPING_LOCATION_SYSTEM_PROPERTY = "unprotect.mappings";

    static final Logger LOGGER = LogManager.getLogger("unprotect");

    private @Nullable Target target;
    private @Nullable TargetCache targetCache;

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

    boolean handlesClass(Type classType, boolean isEmpty) {
        if (isEmpty) {
            return false;
        }

        switch (getTarget()) {
            case ALL:
                return true;
            case MINECRAFT_AND_FORGE:
                String internalName = classType.getInternalName();

                if (internalName.startsWith(Packages.FORGE) || internalName.startsWith(Packages.NEOFORGE)) {
                    return true;
                }

                if (targetCache == null) {
                    targetCache = new TargetCache();
                }

                return targetCache.isMinecraftClass(classType);
            case NONE:
            default:
                return false;
        }
    }

    boolean processClass(ClassNode classNode) {
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
