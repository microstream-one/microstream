
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
 * This annotation indicates the options how the changes will be stored by the MicroStream integration.
 * Storage is done by default in an asynchronous way (to not slow down the user request) and Lazy references
 * are cleared after they are stored.  The member values of this annotation are ignored when the configuration keys
 * xxx FIXME are set. When the configuration key values are set to false, you indicate that at an application level data
 * is stored synchronously and/or Lazy references are not cleared.
 *
 * FIXME
 * The rule is: "The Object that has been modified has to be stored!".
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
	 * Indicate if the instances that are marked as Dirty are stored asynchronously or synchronously. Value is ignored
	 * when the configuration key FIXME
	 * @return if data is stored synchronously or asynchronously.
	 */
	@Nonbinding
	boolean asynchronous() default true;

	/**
	 * Indicates if {@link one.microstream.reference.Lazy} references are cleared after they are stored. Value is
	 * ignored when the configuration key FIXME
	 * @return Clear the Lazy references after storing.
	 */
	@Nonbinding
	boolean clearLazy() default true;
}
