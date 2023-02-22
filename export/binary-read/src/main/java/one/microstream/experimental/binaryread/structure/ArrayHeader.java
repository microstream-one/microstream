package one.microstream.experimental.binaryread.structure;

/*-
 * #%L
 * binary-read
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

/**
 * Data structure holding the header information of an 'array' block.
 */
public class ArrayHeader
{

    private final long totalLength;
    private final long size;

    public ArrayHeader(final long totalLength, final long size)
    {
        this.totalLength = totalLength;
        this.size = size;
    }

    public long getTotalLength()
    {
        return totalLength;
    }

    public long getSize()
    {
        return size;
    }
}
