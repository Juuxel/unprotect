/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect;

import static juuxel.unprotect.UnprotectLaunchPlugin.changeAccess;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

class AccessTest {
    @Test
    void testPackagePrivate() {
        int access1 = 0;
        assertEquals(Opcodes.ACC_PUBLIC, changeAccess(access1));

        int access2 = Opcodes.ACC_ABSTRACT;
        assertEquals(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, changeAccess(access2));
    }

    @Test
    void testProtected() {
        int access1 = Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL;
        assertEquals(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, changeAccess(access1));

        int access2 = Opcodes.ACC_PROTECTED;
        assertEquals(Opcodes.ACC_PUBLIC, changeAccess(access2));
    }

    @Test
    void testPrivateNoOp() {
        int access1 = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
        assertEquals(access1, changeAccess(access1));

        int access2 = Opcodes.ACC_PRIVATE;
        assertEquals(access2, changeAccess(access2));
    }

    @Test
    void testPublicNoOp() {
        int access1 = Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_SYNTHETIC;
        assertEquals(access1, changeAccess(access1));

        int access2 = Opcodes.ACC_PUBLIC;
        assertEquals(access2, changeAccess(access2));
    }
}
