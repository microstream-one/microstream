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

public class Strings {

    private String value;

    private Optional<String> optValue;

    private String[] stringArray;

    private List<String> stringList;

    private Set<String> stringSet;

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public Optional<String> getOptValue() {
        return optValue;
    }

    public void setOptValue(final Optional<String> optValue) {
        this.optValue = optValue;
    }

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(final String[] stringArray) {
        this.stringArray = stringArray;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(final List<String> stringList) {
        this.stringList = stringList;
    }

    public Set<String> getStringSet() {
        return stringSet;
    }

    public void setStringSet(final Set<String> stringSet) {
        this.stringSet = stringSet;
    }
}
