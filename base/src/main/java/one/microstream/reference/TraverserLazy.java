package one.microstream.reference;

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

import one.microstream.util.traversing.AbstractTraversalSkipSignal;
import one.microstream.util.traversing.MutationListener;
import one.microstream.util.traversing.TraversalAcceptor;
import one.microstream.util.traversing.TraversalEnqueuer;
import one.microstream.util.traversing.TraversalMutator;
import one.microstream.util.traversing.TypeTraverser;

public final class TraverserLazy implements TypeTraverser<Lazy<?>>
{
	@SuppressWarnings({"unchecked",  "rawtypes"})
	public static Class<Lazy<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)Lazy.class;
	}
	
	@Override
	public final void traverseReferences(
		final Lazy<?>           instance,
		final TraversalEnqueuer enqueuer
	)
	{
		final boolean wasClear = instance.peek() == null;
		
		try
		{
			enqueuer.enqueue(instance.get());
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		finally
		{
			if(wasClear)
			{
				instance.clear();
			}
		}
	}
	
	@Override
	public final void traverseReferences(
		final Lazy<?>           instance,
		final TraversalEnqueuer enqueuer,
		final TraversalAcceptor acceptor
	)
	{
		final boolean wasClear = instance.peek() == null;
		
		try
		{
			final Object current;
			if(acceptor.acceptReference(current = instance.get(), instance))
			{
				enqueuer.enqueue(current);
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		finally
		{
			if(wasClear)
			{
				instance.clear();
			}
		}
	}
		
	@Override
	public final void traverseReferences(
		final Lazy<?>           instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final boolean wasClear = instance.peek() == null;
		
		try
		{
			final Object current;
			enqueuer.enqueue(current = instance.get());
			if(mutator.mutateReference(current, instance) != current)
			{
				throw new UnsupportedOperationException();
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		finally
		{
			if(wasClear)
			{
				instance.clear();
			}
		}
	}
	
	@Override
	public final void traverseReferences(
		final Lazy<?>           instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final boolean wasClear = instance.peek() == null;
		
		try
		{
			final Object current;
			if(acceptor.acceptReference(current = instance.get(), instance))
			{
				enqueuer.enqueue(current);
			}
			if(mutator.mutateReference(current, instance) != current)
			{
				throw new UnsupportedOperationException();
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		finally
		{
			if(wasClear)
			{
				instance.clear();
			}
		}
	}
	
	@Override
	public final void traverseReferences(
		final Lazy<?>           instance,
		final TraversalAcceptor acceptor
	)
	{
		final boolean wasClear = instance.peek() == null;
		
		try
		{
			acceptor.acceptReference(instance.get(), instance);
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		finally
		{
			if(wasClear)
			{
				instance.clear();
			}
		}
	}
		
	@Override
	public final void traverseReferences(
		final Lazy<?>           instance        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final boolean wasClear = instance.peek() == null;
		
		try
		{
			final Object current = instance.get();
			if(mutator.mutateReference(current, instance) != current)
			{
				throw new UnsupportedOperationException();
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		finally
		{
			if(wasClear)
			{
				instance.clear();
			}
		}
	}
	
	@Override
	public final void traverseReferences(
		final Lazy<?>           instance        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final boolean wasClear = instance.peek() == null;
		
		try
		{
			final Object current;
			acceptor.acceptReference(current = instance.get(), instance);
			if(mutator.mutateReference(current, instance) != current)
			{
				throw new UnsupportedOperationException();
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
		finally
		{
			if(wasClear)
			{
				instance.clear();
			}
		}
	}
	
}
