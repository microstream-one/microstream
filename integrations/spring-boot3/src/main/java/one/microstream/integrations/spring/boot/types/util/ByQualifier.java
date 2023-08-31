package one.microstream.integrations.spring.boot.types.util;

/*-
 * #%L
 * microstream-integrations-spring-boot3
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

import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.stream.Collectors;

public final class ByQualifier
{

    private ByQualifier()
    {
    }

    public static <T> List<T> filter(final List<T> originalList, final String qualifier)
    {
        return originalList.stream()
                .filter(it -> hasQualifierValue(it, qualifier))
                .collect(Collectors.toList());
    }

    public static <T> boolean hasQualifierValue(final T item, final String qualifier)
    {
        boolean result = true; // No qualifier means we need to keep the item
        final Qualifier qualifierAnnotation = item.getClass()
                .getAnnotation(Qualifier.class);
        if (qualifierAnnotation != null && !qualifier.equals(qualifierAnnotation.value())) {
            // We have a Qualifier and its value doesn't match.
            result = false;
        }
        return result;
    }
}
