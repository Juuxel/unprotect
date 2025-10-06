/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package juuxel.unprotect;

import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ProcessorName;

public final class UnprotectClassProcessor implements ClassProcessor {
    private static final ProcessorName NAME = new ProcessorName("unprotect", "processor");

    private final Transformation transformation = new Transformation("FancyModLoader");

    @Override
    public ProcessorName name() {
        return NAME;
    }

    @Override
    public boolean handlesClass(SelectionContext context) {
        return transformation.handlesClass(context.type(), context.empty());
    }

    @Override
    public ComputeFlags processClass(TransformationContext context) {
        return transformation.processClass(context.node()) ? ComputeFlags.SIMPLE_REWRITE : ComputeFlags.NO_REWRITE;
    }
}
