
package one.microstream.integrations.cdi.types;

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;


/**
 * This annotation indicates the operation that will be stored using Microstream automatically.
 * It is a high-level implementation to save either the Iterable and Map instances
 * or the root itself, where you can set by StoreType.
 * By default, it is lazy, and using the EAGER only is extremely necessary.
 * The rule is: "The Object that has been modified has to be stored!".
 * So, to more tuning and optimization in the persistence process,
 * you can always have the option to do it manually through
 * the {@link one.microstream.storage.types.StorageManager#store(Object)} method.
 * Ref: https://docs.microstream.one/manual/storage/storing-data/index.html
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Store
{
	/**
	 * Defines the instance that will call the {@link one.microstream.storage.types.StorageManager#store(Object)}
	 * method where:
	 * EAGER is the whole root
	 * Lazy only the Iterable and Map instances
	 *
	 * @return the {@link StoreType}
	 */
	@Nonbinding
	StoreType value() default StoreType.LAZY;
	
	/**
	 * Define the fields there will be stored.
	 * By default, all iterable or Map domains are targets of the storage method.
	 *
	 * @return the storage fields targets in the
	 */
	@Nonbinding
	String[] fields() default {""};
	
	/**
	 * Define if Microstream will ignore the {@link Store#fields()} and then store the whole entity class root.
	 * By default, it will use the fields.
	 * 
	 * @return it will store the fields or the root class.
	 */
	@Nonbinding
	boolean root() default false;
}
