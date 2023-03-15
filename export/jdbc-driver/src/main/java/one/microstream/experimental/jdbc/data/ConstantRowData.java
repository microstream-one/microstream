package one.microstream.experimental.jdbc.data;

/*-
 * #%L
 * jdbc-driver
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

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation that just holds a single value as Row Data. Used for the Primitive collections
 */
public class ConstantRowData implements RowData
{
    private final Object value;

    public ConstantRowData(final Object value)
    {
        this.value = value;
    }

    @Override
    public List<RowData> getSubRowData(final String name)
    {
        // FIXME Is this correct. Throw an exception when we try the call this as we should never do this?
        return new ArrayList<>();
    }

    @Override
    public Object getValue(final String name)
    {
        // No field name selection as it is used for Primitive collections types that show each
        // item as a row.
        return value;
    }
}
