package one.microstream.typing;

/*-
 * #%L
 * microstream-base
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

import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;


public interface Named
{
	public String name();
	
	
	
	public static <C extends Consumer<? super String>> C toNames(
		final Iterable<? extends Named> items    ,
		final C                         collector
	)
	{
		for(final Named named : items)
		{
			collector.accept(named.name());
		}
		
		return collector;
	}
	
	public static XGettingCollection<String> toNames(final Iterable<? extends Named> items)
	{
		return toNames(items, X.List());
	}
	
}
