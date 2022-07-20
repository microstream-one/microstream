package one.microstream.integrations.spring.boot.types;

/*-
 * #%L
 * microstream-integrations-spring-boot
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Store {

    /**
     * Indicate if the instances that are marked as Dirty are stored asynchronously or synchronously. Value is ignored
     * when the configuration key FIXME
     *
     * @return if data is stored synchronously or asynchronously.
     */
    boolean asynchronous() default true;

    /**
     * Indicates if {@link one.microstream.reference.Lazy} references are cleared after they are stored. Value is
     * ignored when the configuration key FIXME
     *
     * @return Clear the Lazy references after storing.
     */
    boolean clearLazy() default true;
}
