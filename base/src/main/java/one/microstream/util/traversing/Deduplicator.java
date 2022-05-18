package one.microstream.util.traversing;

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

import static one.microstream.X.notNull;

import java.util.function.Function;

import one.microstream.collections.EqHashEnum;


public final class Deduplicator implements Function<Object, Object>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static Deduplicator New()
	{
		return New(
			EqHashEnum.New()
		);
	}
	
	public static Deduplicator New(final EqHashEnum<Object> registry)
	{
		return new Deduplicator(
			notNull(registry)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final EqHashEnum<Object> registry;
	
	
		
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	Deduplicator(final EqHashEnum<Object> registry)
	{
		super();
		this.registry = registry;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Object apply(final Object instance)
	{
		return this.registry.deduplicate(instance);
	}

}
