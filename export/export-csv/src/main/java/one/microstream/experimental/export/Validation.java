package one.microstream.experimental.export;

/*-
 * #%L
 * export
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.afs.types.ADirectory;

public final class Validation {

    private Validation() {
    }

    public static boolean ensureExportDirectoryValidity(final String exportLocation) {
        final NioFileSystem fileSystem = NioFileSystem.New();
        final ADirectory directory = fileSystem.ensureDirectoryPath(exportLocation);
        // The above just ensure the exportLocation has a valid structure and we have a Directory. But is doesn't
        // create it if not exists.
        directory.ensureExists();
        return directory.isEmpty();
    }
}
