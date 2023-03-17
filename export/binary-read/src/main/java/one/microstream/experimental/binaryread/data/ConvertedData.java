package one.microstream.experimental.binaryread.data;

/*-
 * #%L
 * binary-read
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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
 * The result of a DataStorageSerializer.  It might be that the value could not be resolved
 * to a 'simple' type and in that case the reference is returned.
 */
public class ConvertedData
{

    private final Object data;

    private final Long reference;

    private final boolean resolved;

    public ConvertedData(final Object data)
    {
        this.data = data;
        this.reference = null;
        this.resolved = true;
    }

    public ConvertedData(final Long reference, final boolean resolved)
    {
        this.resolved = resolved;
        if (resolved)
        {
            data = reference;
            this.reference = null;

        }
        else
        {
            data = null;
            this.reference = reference;
        }
    }

    public <T> T getData()
    {
        return (T) data;
    }

    public Long getReference()
    {
        return reference;
    }

    public boolean isResolved()
    {
        return resolved;
    }

}
