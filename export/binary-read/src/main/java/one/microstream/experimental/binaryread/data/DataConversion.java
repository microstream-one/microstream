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

import java.math.BigDecimal;
import java.math.BigInteger;

public class DataConversion
{

    public Object perform(final Object value, final String type)
    {
        if (value == null)
        {
            // Nothing to convert
            return null;
        }
        if ("java.math.BigInteger".equals(type) || "[byte]".equals(type))
        {
            return new BigInteger((byte[]) value);
        }
        if ("java.math.BigDecimal".equals(type))
        {
            // TODO Is this always a good way. Value is String for type BigDecimal but can conversion fail?
            return new BigDecimal((String) value);
        }
        if (String.class.equals(value.getClass()))
        {
            return value;
        }
        // TODO How can we find all other cases??

        return value;
    }
}
