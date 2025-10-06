/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * The Unprotect agent.
 *
 * <p>Requires the {@link java.instrument} module.
 *
 * <p>The agent supports all system properties documented in {@link UnprotectLaunchPlugin}.
 */
public final class UnprotectAgent {
    /**
     * The agent entrypoint method.
     *
     * @param agentArgs       the agent options passed to this agent
     * @param instrumentation the instrumentation services
     * @see java.lang.instrument
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        instrumentation.addTransformer(new UnprotectTransformer(), instrumentation.isRetransformClassesSupported());
    }

    private static final class UnprotectTransformer implements ClassFileTransformer {
        private final Transformation transformation = new Transformation("Java agent");

        @Override
        public byte @Nullable [] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (!transformation.handlesClass(Type.getObjectType(className), false)) {
                return null;
            }

            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);

            if (transformation.processClass(node)) {
                ClassWriter writer = new ClassWriter(reader, 0);
                node.accept(writer);
                return writer.toByteArray();
            }

            return null;
        }
    }
}
