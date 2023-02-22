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

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Enums {

    private MyEnum anEnum;

    private MyEnum[] enumArray;

    private Optional<MyEnum> enumOpt;

    private List<MyEnum> enumList;

    private Set<MyEnum> enumSet;

    public MyEnum getAnEnum() {
        return anEnum;
    }

    public void setAnEnum(final MyEnum anEnum) {
        this.anEnum = anEnum;
    }

    public MyEnum[] getEnumArray() {
        return enumArray;
    }

    public void setEnumArray(final MyEnum[] enumArray) {
        this.enumArray = enumArray;
    }

    public Optional<MyEnum> getEnumOpt() {
        return enumOpt;
    }

    public void setEnumOpt(final Optional<MyEnum> enumOpt) {
        this.enumOpt = enumOpt;
    }

    public List<MyEnum> getEnumList() {
        return enumList;
    }

    public void setEnumList(final List<MyEnum> enumList) {
        this.enumList = enumList;
    }

    public Set<MyEnum> getEnumSet() {
        return enumSet;
    }

    public void setEnumSet(final Set<MyEnum> enumSet) {
        this.enumSet = enumSet;
    }
}
