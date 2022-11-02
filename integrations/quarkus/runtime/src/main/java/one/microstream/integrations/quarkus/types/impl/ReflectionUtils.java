package one.microstream.integrations.quarkus.types.impl;

/*-
 * #%L
 * MicroStream Quarkus Extension - Runtime
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ReflectionUtils
{

    private ReflectionUtils()
    {
    }

    public static List<Field> findFields(final Class<?> clazz, final Predicate<Field> predicate)
    {

        return findAllFields(clazz).stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public static List<Field> findAllFields(final Class<?> clazz)
    {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !field.isSynthetic())
                .collect(Collectors.toList());
    }

}
