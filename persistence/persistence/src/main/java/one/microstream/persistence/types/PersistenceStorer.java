package one.microstream.persistence.types;

import static one.microstream.X.notNull;

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

import one.microstream.reference.ObjectSwizzling;
import one.microstream.util.BufferSizeProviderIncremental;

public interface PersistenceStorer extends Storer
{
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public PersistenceStorer initialize();
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public PersistenceStorer initialize(long initialCapacity);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistenceStorer reinitialize();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistenceStorer reinitialize(long initialCapacity);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistenceStorer ensureCapacity(long desiredCapacity);

	public interface Creator<D>
	{
		/**
		 * Creates a {@link PersistenceStorer} instance with a storing logic that stores instances that are
		 * encountered during the traversal of the entity graph that "require" to be stored. The actual meaning
		 * of being "required" depends on the implementation. An example for being "required" is not having an
		 * instance registered in the global object registry and associated an biunique OID.
		 * 
		 * @param typeManager the provided type manager
		 * @param objectManager the provided object manager
		 * @param objectRetriever the provided object retriever
		 * @param target the provided persistence target
		 * @param bufferSizeProvider the provided buffer size provider
		 * @return a new lazy storer
		 */
		public PersistenceStorer createLazyStorer(
			PersistenceTypeHandlerManager<D> typeManager       ,
			PersistenceObjectManager<D>      objectManager     ,
			ObjectSwizzling                  objectRetriever   ,
			PersistenceTarget<D>             target            ,
			BufferSizeProviderIncremental    bufferSizeProvider
		);
		
		/**
		 * Creates a storer with a default or "natural" storing logic. The default for this method
		 * (the "default default" in a way) is to delegate the call to {@link #createLazyStorer}.
		 * 
		 *@param typeManager the provided type manager
		 * @param objectManager the provided object manager
		 * @param objectRetriever the provided object retriever
		 * @param target the provided persistence target
		 * @param bufferSizeProvider the provided buffer size provider
		 * @return a new storer
		 */
		public default PersistenceStorer createStorer(
			final PersistenceTypeHandlerManager<D> typeManager       ,
			final PersistenceObjectManager<D>      objectManager     ,
			final ObjectSwizzling                  objectRetriever   ,
			final PersistenceTarget<D>             target            ,
			final BufferSizeProviderIncremental    bufferSizeProvider
		)
		{
			return this.createLazyStorer(typeManager, objectManager, objectRetriever, target, bufferSizeProvider);
		}
		
		/**
		 * Creates a {@link PersistenceStorer} instance with a storing logic that stores every instance that is
		 * encountered during the traversal of the entity graph once.<br>
		 * Warning: This means that every (persistable) reference is traversed and every reachable instance is stored.
		 * Depending on the used data model, this can mean that the whole entity graph of an application is traversed
		 * and stored. This MIGHT be reasonable for very tiny applications, where storing simply means to start at the
		 * root entity and indiscriminately store every entity there is. Apart from this (rather academic) case,
		 * a storer with this logic should only be used for a confined entity sub-graph that has no reference "escaping"
		 * to the remaning entities.
		 * 
		 * @param typeManager the provided type manager
		 * @param objectManager the provided object manager
		 * @param objectRetriever the provided object retriever
		 * @param target the provided persistence target
		 * @param bufferSizeProvider the provided buffer size provider
		 * @return a new eager storer
		 */
		public PersistenceStorer createEagerStorer(
			PersistenceTypeHandlerManager<D> typeManager       ,
			PersistenceObjectManager<D>      objectManager     ,
			ObjectSwizzling                  objectRetriever   ,
			PersistenceTarget<D>             target            ,
			BufferSizeProviderIncremental    bufferSizeProvider
		);
	}
	
	
	
	@FunctionalInterface
	public interface CreationObserver
	{
		public static void noOp(final PersistenceStorer storer)
		{
			// no-op
		}

		public void observeCreatedStorer(PersistenceStorer storer);


		public static PersistenceStorer.CreationObserver Chain(
			final CreationObserver first ,
			final CreationObserver second
		)
		{
			return new PersistenceStorer.CreationObserver.Chaining(
				notNull(first) ,
				notNull(second)
			);
		}

		public final class Chaining implements PersistenceStorer.CreationObserver
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private final PersistenceStorer.CreationObserver first, second;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Chaining(final CreationObserver first, final CreationObserver second)
			{
				super();
				this.first = first;
				this.second = second;
			}



			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public void observeCreatedStorer(final PersistenceStorer storer)
			{
				this.first.observeCreatedStorer(storer);
				this.second.observeCreatedStorer(storer);
			}

		}
	}

}
