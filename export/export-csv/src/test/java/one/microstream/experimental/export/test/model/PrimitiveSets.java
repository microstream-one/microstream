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

import java.util.Set;

public class PrimitiveSets {

    private Set<Boolean> boolSet;

    private Set<Byte> byteSet;

    private Set<Short> shortSet;

    private Set<Integer> intSet;

    private Set<Long> longSet;

    private Set<Float> floatSet;

    private Set<Double> doubleSet;

    private Set<Character> charSet;

    public Set<Boolean> getBoolSet() {
        return boolSet;
    }

    public void setBoolSet(final Set<Boolean> boolSet) {
        this.boolSet = boolSet;
    }

    public Set<Byte> getByteSet() {
        return byteSet;
    }

    public void setByteSet(final Set<Byte> byteSet) {
        this.byteSet = byteSet;
    }

    public Set<Short> getShortSet() {
        return shortSet;
    }

    public void setShortSet(final Set<Short> shortSet) {
        this.shortSet = shortSet;
    }

    public Set<Integer> getIntSet() {
        return intSet;
    }

    public void setIntSet(final Set<Integer> intSet) {
        this.intSet = intSet;
    }

    public Set<Long> getLongSet() {
        return longSet;
    }

    public void setLongSet(final Set<Long> longSet) {
        this.longSet = longSet;
    }

    public Set<Float> getFloatSet() {
        return floatSet;
    }

    public void setFloatSet(final Set<Float> floatSet) {
        this.floatSet = floatSet;
    }

    public Set<Double> getDoubleSet() {
        return doubleSet;
    }

    public void setDoubleSet(final Set<Double> doubleSet) {
        this.doubleSet = doubleSet;
    }

    public Set<Character> getCharSet() {
        return charSet;
    }

    public void setCharSet(final Set<Character> charSet) {
        this.charSet = charSet;
    }
}
