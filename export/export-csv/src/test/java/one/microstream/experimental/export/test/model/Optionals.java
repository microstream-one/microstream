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

import java.util.Optional;

public class Optionals {

    private Optional<Boolean> optBool;

    private Optional<Byte> optByte;

    private Optional<Short> optShort;

    private Optional<Integer> optInt;

    private Optional<Long> optLong;

    private Optional<Float> optFloat;

    private Optional<Double> optDouble;

    private Optional<Character> optChar;

    public Optional<Boolean> getOptBool() {
        return optBool;
    }

    public void setOptBool(final Optional<Boolean> optBool) {
        this.optBool = optBool;
    }

    public Optional<Byte> getOptByte() {
        return optByte;
    }

    public void setOptByte(final Optional<Byte> optByte) {
        this.optByte = optByte;
    }

    public Optional<Short> getOptShort() {
        return optShort;
    }

    public void setOptShort(final Optional<Short> optShort) {
        this.optShort = optShort;
    }

    public Optional<Integer> getOptInt() {
        return optInt;
    }

    public void setOptInt(final Optional<Integer> optInt) {
        this.optInt = optInt;
    }

    public Optional<Long> getOptLong() {
        return optLong;
    }

    public void setOptLong(final Optional<Long> optLong) {
        this.optLong = optLong;
    }

    public Optional<Float> getOptFloat() {
        return optFloat;
    }

    public void setOptFloat(final Optional<Float> optFloat) {
        this.optFloat = optFloat;
    }

    public Optional<Double> getOptDouble() {
        return optDouble;
    }

    public void setOptDouble(final Optional<Double> optDouble) {
        this.optDouble = optDouble;
    }

    public Optional<Character> getOptChar() {
        return optChar;
    }

    public void setOptChar(final Optional<Character> optChar) {
        this.optChar = optChar;
    }
}
