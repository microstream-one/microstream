package one.microstream.experimental.export.test.model;

/*-
 * #%L
 * export-flat-csv
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

import java.util.HashMap;
import java.util.Map;

public class Maps
{

    private Map<String, String> stringMap = new HashMap<>();
    private Map<String, Long> countMap = new HashMap<>();
    private Map<Double, Double> dataMap = new HashMap<>();

    public Map<String, String> getStringMap()
    {
        return stringMap;
    }

    public void setStringMap(final Map<String, String> stringMap)
    {
        this.stringMap = stringMap;
    }

    public Map<String, Long> getCountMap()
    {
        return countMap;
    }

    public void setCountMap(final Map<String, Long> countMap)
    {
        this.countMap = countMap;
    }

    public Map<Double, Double> getDataMap()
    {
        return dataMap;
    }

    public void setDataMap(final Map<Double, Double> dataMap)
    {
        this.dataMap = dataMap;
    }
}
