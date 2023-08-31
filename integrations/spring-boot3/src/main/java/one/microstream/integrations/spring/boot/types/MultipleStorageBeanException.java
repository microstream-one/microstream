package one.microstream.integrations.spring.boot.types;

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

import org.springframework.beans.BeansException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultipleStorageBeanException extends BeansException
{
    public MultipleStorageBeanException(final Map<String, ? extends List<? extends Class<?>>> classes)
    {
        super(String.format("Multiple beans are annotated with @Storage and the same qualifier: %s",
                            defineInfo(classes)));
    }

    private static String defineInfo(final Map<String, ? extends List<? extends Class<?>>> classes)
    {
        return classes.entrySet()
                .stream()
                .map(entry -> String.format("%s -> %s", entry.getKey(), listClassNames(entry.getValue())))
                .collect(Collectors.joining(" / "));
    }

    private static String listClassNames(List<? extends Class<?>> classes)
    {
        return classes.stream()
                .map(Class::getName)
                .collect(Collectors.joining(","));
    }
}
