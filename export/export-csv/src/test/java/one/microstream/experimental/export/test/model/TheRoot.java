package one.microstream.experimental.export.test.model;

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

import java.util.ArrayList;
import java.util.List;

public class TheRoot
{

    private final List<SomePrimitives> primitives = new ArrayList<>();
    private final List<PrimitiveWrappers> wrappers = new ArrayList<>();
    private final List<PrimitiveArrays> primitiveArrays = new ArrayList<>();
    private final List<PrimitiveWrapperArrays> primitiveWrapperArrays = new ArrayList<>();
    private final List<PrimitiveLists> primitiveLists = new ArrayList<>();
    private final List<PrimitiveSets> primitiveSets = new ArrayList<>();
    private final List<Optionals> optionalList = new ArrayList<>();
    private final List<Enums> enumList = new ArrayList<>();
    private final List<Strings> strings = new ArrayList<>();
    private final List<BigValues> bigValues = new ArrayList<>();
    private final List<Dates> dateValues = new ArrayList<>();
    private final List<Maps> maps = new ArrayList<>();

    public void addSomePrimitives(final SomePrimitives somePrimitives)
    {
        primitives.add(somePrimitives);
    }

    public List<SomePrimitives> getPrimitives()
    {
        return primitives;
    }

    public void addPrimitiveWrappers(final PrimitiveWrappers primitiveWrappers)
    {
        wrappers.add(primitiveWrappers);
    }

    public List<PrimitiveWrappers> getWrappers()
    {
        return wrappers;
    }

    public void addPrimitiveArrays(final PrimitiveArrays primitiveArrays)
    {
        this.primitiveArrays.add(primitiveArrays);
    }

    public List<PrimitiveArrays> getPrimitiveArrays()
    {
        return primitiveArrays;
    }

    public void addPrimitiveWrapperArrays(final PrimitiveWrapperArrays primitiveWrapperArrays)
    {
        this.primitiveWrapperArrays.add(primitiveWrapperArrays);

    }

    public List<PrimitiveWrapperArrays> getPrimitiveWrapperArrays()
    {
        return primitiveWrapperArrays;
    }

    public void addPrimitiveLists(final PrimitiveLists primitiveLists)
    {
        this.primitiveLists.add(primitiveLists);
    }

    public List<PrimitiveLists> getPrimitiveLists()
    {
        return primitiveLists;
    }

    public void addPrimitiveSets(final PrimitiveSets primitiveSets)
    {
        this.primitiveSets.add(primitiveSets);
    }

    public List<PrimitiveSets> getPrimitiveSets()
    {
        return primitiveSets;
    }

    public void addOptionals(final Optionals optionals)
    {
        this.optionalList.add(optionals);
    }

    public List<Optionals> getOptionalList()
    {
        return optionalList;
    }

    public void addStrings(final Strings strings)
    {
        this.strings.add(strings);
    }

    public List<Strings> getStrings()
    {
        return strings;
    }

    public void addEnums(final Enums enums)
    {
        this.enumList.add(enums);
    }

    public List<Enums> getEnumList()
    {
        return enumList;
    }

    public void addBigValues(final BigValues values)
    {
        this.bigValues.add(values);
    }

    public List<BigValues> getBigValues()
    {
        return bigValues;
    }

    public void addDateValues(final Dates dates)
    {
        this.dateValues.add(dates);
    }

    public List<Dates> getDateValues()
    {
        return dateValues;
    }

    public void addMaps(final Maps maps)
    {
        this.maps.add(maps);
    }

    public List<Maps> getMaps()
    {
        return maps;
    }
}
