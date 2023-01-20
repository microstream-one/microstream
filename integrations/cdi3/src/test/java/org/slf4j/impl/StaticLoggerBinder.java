/*
 * Copyright 2021-2022 Rudy De Busscher (https://www.atbash.be)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.slf4j.impl;

/*-
 * #%L
 * microstream-integrations-cdi3
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

import one.microstream.integrations.cdi.types.logging.TestLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder
{

    /**
     * Declare the version of the SLF4J API this implementation is compiled against. The value of
     * this field is usually modified with each release. Per SLF4J,
     * "To avoid constant folding by the compiler, this field must *not* be final".
     */
    public static String REQUESTED_API_VERSION = "1.7";

    /**
     * Logger factory class.
     */
    private static final Class<? extends ILoggerFactory> loggerFactoryClass =
            TestLoggerFactory.class;

    /**
     * The unique instance of this class.
     */
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    /**
     * The ILoggerFactory instance returned by the {@link #getLoggerFactory} method should always be
     * the same object.
     */
    private final ILoggerFactory loggerFactory;

    /**
     * Instantiates a new static logger binder.
     */
    private StaticLoggerBinder()
    {

            loggerFactory = new TestLoggerFactory();

    }

    /**
     * Return the singleton of this class.
     *
     * @return the StaticLoggerBinder singleton
     */
    public static StaticLoggerBinder getSingleton()
    {
        return SINGLETON;
    }

    @Override
    public ILoggerFactory getLoggerFactory()
    {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr()
    {
        return loggerFactoryClass.getName();
    }
}
