/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.jetbrains.annotations.VisibleForTesting;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.EnumSet;

public final class UnprotectLaunchPlugin implements ILaunchPluginService {
    // Package-private doesn't have its own access flag and is used when there's
    // none of these other flags.
    private static final int ACCESS_MASK = Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE;

    @Override
    public String name() {
        return "unprotect";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        return !isEmpty ? EnumSet.of(Phase.AFTER) : EnumSet.noneOf(Phase.class);
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
}
