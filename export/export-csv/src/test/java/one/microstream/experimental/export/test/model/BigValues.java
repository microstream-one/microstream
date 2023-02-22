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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BigValues {

    private BigInteger aBigInteger;
    private BigDecimal aBigDecimal;

    private BigInteger[] bigIntegerArray;
    private BigDecimal[] bigDecimalArray;

    private Optional<BigInteger> bigIntegerOpt;
    private Optional<BigDecimal> bigDecimalOpt;

    private List<BigInteger> bigIntegerList;
    private List<BigDecimal> bigDecimalList;

    private Set<BigInteger> bigIntegerSet;
    private Set<BigDecimal> bigDecimalSet;


    public BigInteger getaBigInteger() {
        return aBigInteger;
    }

    public void setaBigInteger(final BigInteger aBigInteger) {
        this.aBigInteger = aBigInteger;
    }

    public BigDecimal getaBigDecimal() {
        return aBigDecimal;
    }

    public void setaBigDecimal(final BigDecimal aBigDecimal) {
        this.aBigDecimal = aBigDecimal;
    }

    public BigInteger[] getBigIntegerArray() {
        return bigIntegerArray;
    }

    public void setBigIntegerArray(final BigInteger[] bigIntegerArray) {
        this.bigIntegerArray = bigIntegerArray;
    }

    public BigDecimal[] getBigDecimalArray() {
        return bigDecimalArray;
    }

    public void setBigDecimalArray(final BigDecimal[] bigDecimalArray) {
        this.bigDecimalArray = bigDecimalArray;
    }

    public Optional<BigInteger> getBigIntegerOpt() {
        return bigIntegerOpt;
    }

    public void setBigIntegerOpt(final Optional<BigInteger> bigIntegerOpt) {
        this.bigIntegerOpt = bigIntegerOpt;
    }

    public Optional<BigDecimal> getBigDecimalOpt() {
        return bigDecimalOpt;
    }

    public void setBigDecimalOpt(final Optional<BigDecimal> bigDecimalOpt) {
        this.bigDecimalOpt = bigDecimalOpt;
    }

    public List<BigInteger> getBigIntegerList() {
        return bigIntegerList;
    }

    public void setBigIntegerList(final List<BigInteger> bigIntegerList) {
        this.bigIntegerList = bigIntegerList;
    }

    public List<BigDecimal> getBigDecimalList() {
        return bigDecimalList;
    }

    public void setBigDecimalList(final List<BigDecimal> bigDecimalList) {
        this.bigDecimalList = bigDecimalList;
    }

    public Set<BigInteger> getBigIntegerSet() {
        return bigIntegerSet;
    }

    public void setBigIntegerSet(final Set<BigInteger> bigIntegerSet) {
        this.bigIntegerSet = bigIntegerSet;
    }

    public Set<BigDecimal> getBigDecimalSet() {
        return bigDecimalSet;
    }

    public void setBigDecimalSet(final Set<BigDecimal> bigDecimalSet) {
        this.bigDecimalSet = bigDecimalSet;
    }
}
