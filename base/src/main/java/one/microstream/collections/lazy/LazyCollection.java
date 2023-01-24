package one.microstream.collections.lazy;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import one.microstream.reference.Lazy;

/**
 * A {@link Collection}, which uses {@link Lazy} references internally,
 * to enable automatic partial loading of the collection's content.
 *
 * @param <E> the type of elements in this collection
 */
public interface LazyCollection<E> extends Collection<E>
{
	/**
	 * Iterates over all internally used {@link Lazy} references.
	 * 
	 * @param <P> the procedure type
	 * @param procedure the lazy reference consumer
	 * @return the given procedure
	 */
	public <P extends Consumer<Lazy<?>>> P iterateLazyReferences(P procedure);
	
	/**
	 * Optimizes the internal structure of this lazy collection.
	 * Closes possible gaps and cleans up unused storage space.
	 * 
	 * @return <code>true</code> if the internal structure has changed, <code>false</code> otherwise
	 */
	public boolean consolidate();
	
	/**
	 * Returns a sequential {@code Stream} with this collection as its source.
	 * <p>
	 * This stream comes with a custom close handler, which clears all {@link Lazy} references
	 * of this collection, which were not loaded before.
	 * So if you want to close all {@link Lazy} references which were not loaded before the
	 * {@link Stream} was created, just call {@link Stream#close()} afterwards.
	 *
	 * @return a sequential {@code Stream} over the elements in this collection
	 */
	@Override
	public default Stream<E> stream()
	{
		return Collection.super.stream()
			.onClose(Static.createLazyStreamCloseHandler(this))
		;
	}
	
	/**
	 * Returns a parallel {@code Stream} with this collection as its source.
	 * <p>
	 * This stream comes with a custom close handler, which clears all {@link Lazy} references
	 * of this collection, which were not loaded before.
	 * So if you want to close all {@link Lazy} references which were not loaded before the
	 * {@link Stream} was created, just call {@link Stream#close()} afterwards.
	 *
	 * @return a parallel {@code Stream} over the elements in this collection
	 */
	@Override
	public default Stream<E> parallelStream()
	{
		return Collection.super.parallelStream()
			.onClose(Static.createLazyStreamCloseHandler(this))
		;
	}
	
	
	public final static class Static
	{
		static Runnable createLazyStreamCloseHandler(final LazyCollection<?> collection)
		{
			final Set<Lazy<?>> unloadedLazyReferences = new HashSet<>();
			collection.iterateLazyReferences(lazy ->
			{
				if(!lazy.isLoaded())
				{
					unloadedLazyReferences.add(lazy);
				}
			});
			return () -> unloadedLazyReferences.forEach(lazy ->
			{
				if(lazy.isLoaded() && lazy.isStored())
				{
					lazy.clear();
				}
			});
		}
		
	}
	
}
