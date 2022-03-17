
package one.microstream.integrations.cdi.types.test;

/*-
 * #%L
 * microstream-integrations-cdi
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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * The CDI extension to work with JUnit 5 Jupiter.
 */
@Target(TYPE)
@Retention(RUNTIME)
@ExtendWith(CDIJUnitExtension.class)
public @interface CDIExtension
{
	/**
	 * @return classes to deploy.
	 */
	Class<?>[] classes() default {};
	
	/**
	 * @return decorators to activate.
	 */
	Class<?>[] decorators() default {};
	
	/**
	 * @return interceptors to activate.
	 */
	Class<?>[] interceptors() default {};
	
	/**
	 * @return alternatives to activate.
	 */
	Class<?>[] alternatives() default {};
	
	/**
	 * @return stereotypes to activate.
	 */
	Class<? extends Annotation>[] alternativeStereotypes() default {};
	
	/**
	 * @return packages to deploy.
	 */
	Class<?>[] packages() default {};
	
	/**
	 * @return packages to deploy recursively.
	 */
	Class<?>[] recursivePackages() default {};
	
	/**
	 * @return if the automatic scanning must be disabled.
	 */
	boolean disableDiscovery() default false;
	
}
