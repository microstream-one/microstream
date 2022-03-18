package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import static one.microstream.X.mayNull;

import java.util.function.Supplier;

import one.microstream.reference.Reference;

public interface PersistenceRootReference extends PersistenceRootReferencing, Reference<Object>
{
	@Override
	public Object get();

	@Override
	public <F extends PersistenceFunction> F iterate(F iterator);
	
	@Override
	public default void set(final Object newRoot)
	{
		this.setRoot(newRoot);
	}
	
	public default Object setRoot(final Object newRoot)
	{
		return this.setRootSupplier(() ->
			newRoot
		);
	}
	
	public Object setRootSupplier(Supplier<?> rootSupplier);
	
	

	public static PersistenceRootReference New()
	{
		return New(null);
	}
	
	public static PersistenceRootReference New(final Object root)
	{
		final PersistenceRootReference.Default instance = new PersistenceRootReference.Default(null);
		instance.setRoot(root);
		
		return instance;
	}
	
	public static PersistenceRootReference New(final Supplier<?> rootSupplier)
	{
		return new PersistenceRootReference.Default(
			mayNull(rootSupplier)
		);
	}
	
	public final class Default implements PersistenceRootReference
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// there is no problem that cannot be solved through one more level of indirection
		private Supplier<?> rootSupplier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		public Default(final Supplier<?> rootSupplier)
		
		{
			super();
			this.rootSupplier = rootSupplier;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Object get()
		{
			return this.rootSupplier != null
				? this.rootSupplier.get()
				: null
			;
		}
		
		@Override
		public final Object setRootSupplier(final Supplier<?> rootSupplier)
		{
			final Object currentRoot = this.get();
			this.rootSupplier = rootSupplier;
			
			return currentRoot;
		}
		
		@Override
		public final <F extends PersistenceFunction> F iterate(final F iterator)
		{
			final Object currentRoot = this.get();
			if(currentRoot == null)
			{
				return iterator;
			}
			iterator.apply(currentRoot);
			
			return iterator;
		}
		
	}
	
}
