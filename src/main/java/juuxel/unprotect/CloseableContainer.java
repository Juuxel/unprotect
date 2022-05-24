/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

/**
 * A container for a closeable value together with its closeable dependencies
 * (e.g. an input stream in a zip).
 * Closing the container will close the value
 * as well as the dependencies.
 *
 * @param <T> the contained value type
 */
final class CloseableContainer<T extends Closeable> implements Closeable {
    final T value;
    private final Closeable[] dependencies;

    private CloseableContainer(T value, Closeable... dependencies) {
        this.value = value;
        this.dependencies = Arrays.copyOf(dependencies, dependencies.length);
    }

    @Contract("null, _ -> null; !null, _ -> !null")
    static <T extends Closeable> @Nullable CloseableContainer<T> of(@Nullable T value, Closeable... dependencies) {
        return value != null ? new CloseableContainer<>(value, dependencies) : null;
    }

    @Override
    public void close() throws IOException {
        @Nullable IOException top = null;

        try {
            value.close();
        } catch (IOException e) {
            top = e;
        }

        for (Closeable dependency : dependencies) {
            try {
                dependency.close();
            } catch (IOException e) {
                if (top == null) {
                    top = e;
                } else {
                    top.addSuppressed(e);
                }
            }
        }

        if (top != null) {
            throw top;
        }
    }
}
