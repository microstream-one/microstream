
package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import javax.cache.processor.EntryProcessorException;


public interface EntryProcessorResult<T> extends javax.cache.processor.EntryProcessorResult<T>
{
	public static <T> EntryProcessorResult<T> New(final T value)
	{
		return () -> value;
	}
	
	public static <T> EntryProcessorResult<T> New(final Exception e)
	{
		final EntryProcessorException epe = e instanceof EntryProcessorException
			? (EntryProcessorException)e
			: new EntryProcessorException(e);
		return () -> {
			throw epe;
		};
	}
}
