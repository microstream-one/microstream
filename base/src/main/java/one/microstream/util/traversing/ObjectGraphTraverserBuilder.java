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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.ConstHashEnum;
import one.microstream.collections.ConstHashTable;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSet;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XMap;
import one.microstream.collections.types.XReplacingBag;
import one.microstream.collections.types.XSet;
import one.microstream.collections.types.XTable;


public interface ObjectGraphTraverserBuilder
{
	public ObjectGraphTraverser buildObjectGraphTraverser();
	
	public TraversalFilter<TraversalPredicateSkip> skip();
	
	public TraversalFilter<TraversalPredicateNode> node();
	
	public TraversalFilter<TraversalPredicateLeaf> leaf();
	
	public TraversalFilter<TraversalPredicateFull> full();
	
	public XTable<Object, TypeTraverser<?>> traversersPerInstance();

	public XTable<Class<?>, TypeTraverser<?>> traversersPerConcreteType();

	public XTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType();
	
	public TraversalFieldSelector fieldSelector();
	
	public Predicate<? super Field> fieldPredicate();

	public Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider();

	public TypeTraverser.Creator typeTraverserCreator();

	public TraversalAcceptor acceptor();

	public Predicate<Object> acceptorPredicate();

	public Consumer<Object> acceptorLogic();

	public TraversalMutator mutator();

	public Predicate<Object> mutatorPredicate();

	public Function<Object, ?> mutatorLogic();

	public MutationListener mutationListener();

	public TraversalMode traversalMode();

	public TraversalReferenceHandlerProvider referenceHandlerProvider();

	public Object[] roots();


	public ObjectGraphTraverserBuilder fieldSelector(TraversalFieldSelector fieldSelector);
	
	public ObjectGraphTraverserBuilder fieldPredicate(Predicate<? super Field> predicate);

	public ObjectGraphTraverserBuilder alreadyHandledProvider(Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider);

	public ObjectGraphTraverserBuilder typeTraverserCreator(TypeTraverser.Creator typeTraverserCreator);

	public ObjectGraphTraverserBuilder acceptor(TraversalAcceptor acceptor);

	public ObjectGraphTraverserBuilder acceptorPredicate(Predicate<Object> acceptorPredicate);

	public ObjectGraphTraverserBuilder acceptorLogic(Consumer<Object> acceptorLogic);

	public ObjectGraphTraverserBuilder mutator(TraversalMutator mutator);

	public ObjectGraphTraverserBuilder mutatorPredicate(Predicate<Object> mutatorPredicate);

	public ObjectGraphTraverserBuilder mutatorLogic(Function<Object, Object> mutatorLogic);

	public ObjectGraphTraverserBuilder mutationListener(MutationListener mutationListener);

	public ObjectGraphTraverserBuilder traversalMode(TraversalMode traversalMode);

	public ObjectGraphTraverserBuilder referenceHandlerProvider(TraversalReferenceHandlerProvider referenceHandlerProvider);

	public ObjectGraphTraverserBuilder roots(Object... roots);
	
	
	
		
	public default ObjectGraphTraverserBuilder modeNode()
	{
		// tiny instantiation instead of permanent memory consumption by a constant.
		return this.traversalMode(new TraversalMode.Node());
	}
	
	public default ObjectGraphTraverserBuilder modeFull()
	{
		// tiny instantiation instead of permanent memory consumption by a constant.
		return this.traversalMode(new TraversalMode.Full());
	}
	
	public default ObjectGraphTraverserBuilder modeLeaf()
	{
		// tiny instantiation instead of permanent memory consumption by a constant.
		return this.traversalMode(new TraversalMode.Leaf());
	}
	
	public default ObjectGraphTraverserBuilder apply(final Consumer<Object> logic)
	{
		return this.apply((Predicate<Object>)null, logic);
	}
	
	public ObjectGraphTraverserBuilder apply(Predicate<Object> predicate, Consumer<Object> logic);
	
	public <T> ObjectGraphTraverserBuilder apply(Class<T> type, Consumer<? super T> logic);
	
	public default ObjectGraphTraverserBuilder mutate(final Function<Object, Object> logic)
	{
		return this.mutate((Predicate<Object>)null, logic);
	}
	
	public ObjectGraphTraverserBuilder mutate(Predicate<Object> predicate, Function<Object, ?> logic);
	
	public <T> ObjectGraphTraverserBuilder mutate(Class<T> type, Function<? super T, ?> logic);
	
	
	public ObjectGraphTraverserBuilder initializerLogic(Runnable logic);
	
	public Runnable initializerLogic();
	
	
	public ObjectGraphTraverserBuilder finalizerLogic(Runnable logic);
	
	public Runnable finalizerLogic();
	

	
	public default ObjectGraphTraverserBuilder root(final Object root)
	{
		return this.roots(root);
	}
	
	public default <T> ObjectGraphTraverserBuilder registerTraverser(final Object instance, final TypeTraverser<T> traverser)
	{
		synchronized(this)
		{
			this.traversersPerInstance().add(instance, traverser);
		}
		return this;
	}
	
	public default <T> ObjectGraphTraverserBuilder registerTraverserForType(final Class<? extends T> type, final TypeTraverser<T> traverser)
	{
		synchronized(this)
		{
			this.traversersPerConcreteType().add(type, traverser);
		}
		return this;
	}
	
	public default <T> ObjectGraphTraverserBuilder registerTraverserForTypePolymorphic(final Class<? extends T> type, final TypeTraverser<T> traverser)
	{
		synchronized(this)
		{
			this.traversersPerPolymorphType().add(type, traverser);
		}
		return this;
	}
	
	public TraversalReferenceHandlerProvider provideReferenceHandlerProvider();
	
	public TraversalAcceptor provideAcceptor();
	
	public TraversalMutator provideMutator();
	
	public XGettingSet<Object> provideSkippedInstances();
	
	public Predicate<Object> predicateHandle();
	
	public ObjectGraphTraverserBuilder predicateHandle(Predicate<Object> predicate);
	
	
	
	
	public static XGettingSet<Class<?>> defaultSkipTypesConcrete()
	{
		/*
		 * Types that are actually value types with no or unshared references as a storage (usually an array).
		 *
		 * System types that are hardly ever desired to be iterated further. If they have to be, they can be
		 * counter-registered as a full, node or leaf type.
		 *
		 * This list is provisional and definitely not complete. Missing types can be added explicitly in the builder.
		 */
		return ConstHashEnum.New(
			Byte.class         ,
			Boolean.class      ,
			Short.class        ,
			Character.class    ,
			Integer.class      ,
			Float.class        ,
			Long.class         ,
			Double.class       ,

			Instant.class      ,

			String.class       ,
			StringBuilder.class,
			StringBuffer.class ,

			Class.class        ,
			Field.class        ,
			Method.class
		);
	}
	
	public static XGettingEnum<Class<?>> defaultSkipTypesPolymorphic()
	{
		/*
		 * Types that are actually value types with no or unshared references as a storage (usually an array).
		 *
		 * System types that are hardly ever desired to be iterated further. If they have to be, they can be
		 * counter-registered as a full, node or leaf type.
		 *
		 * This list is provisional and definitely not complete. Missing types can be added explicitly in the builder.
		 */
		return ConstHashEnum.New(
			BigInteger.class   ,
			BigDecimal.class   ,

			Date.class         ,
			File.class         ,

			Thread.class       ,
			OutputStream.class ,
			InputStream.class  ,
			FileChannel.class  ,
			ByteBuffer.class   ,
			Throwable.class
		);
	}
	
	public static XGettingTable<Class<?>, TypeTraverser<?>> defaultPolymorphTypeTraversers()
	{
		return ConstHashTable.<Class<?>, TypeTraverser<?>>New(
			X.KeyValue(XMap              .class, new TraverserXMapReplacing()          ),
			X.KeyValue(XGettingMap       .class, new TraverserXMapNonReplacing()       ),
			X.KeyValue(XReplacingBag     .class, new TraverserXCollectionReplacing()   ),
			X.KeyValue(XGettingCollection.class, new TraverserXCollectionNonReplacing()),
			X.KeyValue(Collection        .class, new TraverserCollectionOld()          ),
			X.KeyValue(Object[]          .class, new TraverserArray()                  )
		);
	}
	
	public static XGettingTable<Class<?>, TypeTraverser<?>> defaultConcreteTypeTraversers()
	{
		return ConstHashTable.<Class<?>, TypeTraverser<?>>New(
			/* empty so far */
		);
	}
	
	
	
	
	
	public static ObjectGraphTraverserBuilder New()
	{
		return new ObjectGraphTraverserBuilder.Default();
	}
		
	public class Default implements ObjectGraphTraverserBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final TraversalFilter<TraversalPredicateSkip> skip = TraversalFilter.New(this);
		private final TraversalFilter<TraversalPredicateNode> node = TraversalFilter.New(this);
		private final TraversalFilter<TraversalPredicateLeaf> leaf = TraversalFilter.New(this);
		private final TraversalFilter<TraversalPredicateFull> full = TraversalFilter.New(this);
		
		private final HashTable<Object, TypeTraverser<?>>          traversersPerInstance     ;
		private final HashTable<Class<?>, TypeTraverser<?>>        traversersPerConcreteType ;
		private final HashTable<Class<?>, TypeTraverser<?>>        traversersPerPolymorphType;
		
		private TraversalFieldSelector                             fieldSelector             ;
		private Predicate<? super Field>                           fieldPredicate            ;
		private Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider    ;
		private TypeTraverser.Creator                              typeTraverserCreator      ;

		private TraversalAcceptor                                  acceptor                  ;
		private Predicate<Object>                                  acceptorPredicate         ;
		private Consumer<Object>                                   acceptorLogic             ;

		private TraversalMutator                                   mutator                   ;
		private Predicate<Object>                                  mutatorPredicate          ;
		private Function<Object, ?>                                mutatorLogic              ;
		
		private MutationListener                                   mutationListener          ;
		private TraversalMode                                      traversalMode             ;
		private TraversalReferenceHandlerProvider                  referenceHandlerProvider  ;
		private Object[]                                           roots                     ;
		private Predicate<Object>                                  predicateHandle           ;
		
		private Runnable                                           initializerLogic          ;
		private Runnable                                           finalizerLogic            ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
			
			this.skip().types(ObjectGraphTraverserBuilder.defaultSkipTypesConcrete());
			this.skip().typesPolymorphic(ObjectGraphTraverserBuilder.defaultSkipTypesPolymorphic());
						
			this.traversersPerInstance      = HashTable.New();
			this.traversersPerConcreteType  = HashTable.New(
				ObjectGraphTraverserBuilder.defaultConcreteTypeTraversers()
			);
			this.traversersPerPolymorphType = HashTable.New(
				ObjectGraphTraverserBuilder.defaultPolymorphTypeTraversers())
			;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public synchronized ObjectGraphTraverserBuilder initializerLogic(final Runnable logic)
		{
			this.initializerLogic = logic;
			return this;
		}
		
		@Override
		public synchronized Runnable initializerLogic()
		{
			return this.initializerLogic;
		}
		
		
		@Override
		public synchronized ObjectGraphTraverserBuilder finalizerLogic(final Runnable logic)
		{
			this.finalizerLogic = logic;
			return this;
		}
		
		@Override
		public synchronized Runnable finalizerLogic()
		{
			return this.finalizerLogic;
		}
		
		@Override
		public synchronized Predicate<Object> predicateHandle()
		{
			return this.predicateHandle;
		}
		
		@Override
		public synchronized ObjectGraphTraverserBuilder predicateHandle(final Predicate<Object> predicate)
		{
			this.predicateHandle = predicate;
			return this;
		}
		
		@Override
		public synchronized TraversalFilter<TraversalPredicateSkip> skip()
		{
			return this.skip;
		}
		
		@Override
		public synchronized TraversalFilter<TraversalPredicateNode> node()
		{
			return this.node;
		}
		
		@Override
		public synchronized TraversalFilter<TraversalPredicateLeaf> leaf()
		{
			return this.leaf;
		}
		
		@Override
		public synchronized TraversalFilter<TraversalPredicateFull> full()
		{
			return this.full;
		}
		
		@Override
		public synchronized HashTable<Object, TypeTraverser<?>> traversersPerInstance()
		{
			return this.traversersPerInstance;
		}

		@Override
		public synchronized HashTable<Class<?>, TypeTraverser<?>> traversersPerConcreteType()
		{
			return this.traversersPerConcreteType;
		}

		@Override
		public synchronized HashTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType()
		{
			return this.traversersPerPolymorphType;
		}
		
		@Override
		public synchronized TraversalFieldSelector fieldSelector()
		{
			return this.fieldSelector;
		}

		@Override
		public synchronized Predicate<? super Field> fieldPredicate()
		{
			return this.fieldPredicate;
		}

		@Override
		public synchronized Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider()
		{
			return this.alreadyHandledProvider;
		}

		@Override
		public synchronized TypeTraverser.Creator typeTraverserCreator()
		{
			return this.typeTraverserCreator;
		}

		@Override
		public synchronized TraversalAcceptor acceptor()
		{
			return this.acceptor;
		}

		@Override
		public synchronized Predicate<Object> acceptorPredicate()
		{
			return this.acceptorPredicate;
		}

		@Override
		public synchronized Consumer<Object> acceptorLogic()
		{
			return this.acceptorLogic;
		}

		@Override
		public synchronized TraversalMutator mutator()
		{
			return this.mutator;
		}

		@Override
		public synchronized Predicate<Object> mutatorPredicate()
		{
			return this.mutatorPredicate;
		}

		@Override
		public synchronized Function<Object, ?> mutatorLogic()
		{
			return this.mutatorLogic;
		}

		@Override
		public synchronized MutationListener mutationListener()
		{
			return this.mutationListener;
		}

		@Override
		public synchronized TraversalMode traversalMode()
		{
			return this.traversalMode;
		}

		@Override
		public synchronized TraversalReferenceHandlerProvider referenceHandlerProvider()
		{
			return this.referenceHandlerProvider;
		}

		@Override
		public synchronized Object[] roots()
		{
			return this.roots;
		}
		
		@Override
		public synchronized ObjectGraphTraverserBuilder fieldSelector(
			final TraversalFieldSelector fieldSelector
		)
		{
			this.fieldSelector = fieldSelector;
			return this;
		}
		
		@Override
		public synchronized ObjectGraphTraverserBuilder fieldPredicate(
			final Predicate<? super Field> traversableFieldSelector
		)
		{
			this.fieldPredicate = traversableFieldSelector;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder alreadyHandledProvider(
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider
		)
		{
			this.alreadyHandledProvider = alreadyHandledProvider;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder typeTraverserCreator(
			final TypeTraverser.Creator typeTraverserCreator
		)
		{
			this.typeTraverserCreator = typeTraverserCreator;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder acceptor(final TraversalAcceptor acceptor)
		{
			this.acceptor = acceptor;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder acceptorPredicate(final Predicate<Object> acceptorPredicate)
		{
			this.acceptorPredicate = acceptorPredicate;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder acceptorLogic(final Consumer<Object> acceptorLogic)
		{
			this.acceptorLogic = acceptorLogic;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder mutator(final TraversalMutator mutator)
		{
			this.mutator = mutator;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder mutatorPredicate(final Predicate<Object> mutatorPredicate)
		{
			this.mutatorPredicate = mutatorPredicate;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder mutatorLogic(final Function<Object, Object> mutatorLogic)
		{
			this.mutatorLogic = mutatorLogic;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder mutationListener(final MutationListener mutationListener)
		{
			this.mutationListener = mutationListener;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder traversalMode(final TraversalMode traversalMode)
		{
			this.traversalMode = traversalMode;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder referenceHandlerProvider(final TraversalReferenceHandlerProvider referenceHandlerProvider)
		{
			this.referenceHandlerProvider = referenceHandlerProvider;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder roots(final Object... roots)
		{
			this.roots = roots;
			return this;
		}
				
		@Override
		public synchronized ObjectGraphTraverserBuilder mutate(final Predicate<Object> predicate, final Function<Object, ?> logic)
		{
			this.mutatorPredicate = predicate;
			this.mutatorLogic     = logic    ;
			return this;
		}
		
		@Override
		public synchronized <T> ObjectGraphTraverserBuilder mutate(final Class<T> type, final Function<? super T, ?> logic)
		{
			return this.mutate(
				type::isInstance,
				instance ->
					logic.apply(type.cast(instance)
				)
			);
		}
		
		@Override
		public synchronized ObjectGraphTraverserBuilder apply(final Predicate<Object> predicate, final Consumer<Object> logic)
		{
			this.acceptorPredicate = predicate;
			this.acceptorLogic     = logic    ;
			return this;
		}
		
		@Override
		public synchronized <T> ObjectGraphTraverserBuilder apply(final Class<T> type, final Consumer<? super T> logic)
		{
			return this.apply(
				type::isInstance,
				instance ->
					logic.accept(type.cast(instance)
				)
			);
		}
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// provider methods //
		/////////////////////
		
		protected synchronized TypeTraverserProvider provideTypeTraverserProvider()
		{
			return TypeTraverserProvider.New(
				this.provideTypeTraverserCreator(),
				this.traversersPerInstance()      ,
				this.traversersPerConcreteType()  ,
				this.traversersPerPolymorphType()
			);
		}
				
		protected synchronized MutationListener provideMutationListener()
		{
			return this.mutationListener;
		}
		
		public synchronized ObjectGraphTraverserBuilder setMutationListener(
			final MutationListener mutationListener
		)
		{
			this.mutationListener = mutationListener;
			return this;
		}
			
		protected synchronized Function<XGettingCollection<Object>, XSet<Object>>  provideAlreadyHandledProvider()
		{
			if(this.alreadyHandledProvider != null)
			{
				return this.alreadyHandledProvider;
			}

			return OpenAdressingMiniSet::New;
		}
				
		@Override
		public synchronized TraversalAcceptor provideAcceptor()
		{
			if(this.acceptor != null)
			{
				return this.acceptor;
			}
			
			if(this.acceptorLogic != null)
			{
				return this.acceptorPredicate != null
					? TraversalAcceptor.New(this.acceptorPredicate, this.acceptorLogic)
					: TraversalAcceptor.New(this.acceptorLogic)
				;
			}
			
			return null;
		}
		
		@Override
		public synchronized TraversalMutator provideMutator()
		{
			if(this.mutator != null)
			{
				return this.mutator;
			}
			
			if(this.mutatorLogic != null)
			{
				return this.mutatorPredicate != null
					? TraversalMutator.New(this.mutatorPredicate, this.mutatorLogic)
					: TraversalMutator.New(this.mutatorLogic)
				;
			}
			
			return null;
		}
				
		protected synchronized TraversalFieldSelector provideTraversableFieldSelector()
		{
			if(this.fieldSelector != null)
			{
				return this.fieldSelector;
			}
			
			if(this.fieldPredicate != null)
			{
				return TraversalFieldSelector.New(this.fieldPredicate);
			}
			
			return null; // let specific logic decide on its default behavior.
		}
		
		protected synchronized TypeTraverser.Creator provideTypeTraverserCreator()
		{
			if(this.typeTraverserCreator == null)
			{
				this.typeTraverserCreator = TraverserReflective.Creator(
					this.provideTraversableFieldSelector()
				);
			}
			
			return this.typeTraverserCreator;
		}
		
		@Override
		public synchronized TraversalReferenceHandlerProvider provideReferenceHandlerProvider()
		{
			if(this.referenceHandlerProvider != null)
			{
				return this.referenceHandlerProvider;
			}
			return TraversalReferenceHandlerProvider.New();
		}
		
		@Override
		public synchronized XGettingSet<Object> provideSkippedInstances()
		{
			return this.skip().instances();
		}
		
		protected synchronized TraversalPredicateSkip providePredicateSkip()
		{
			final TraversalPredicateSkip explicitPredicate = this.skip.predicate();
			if(explicitPredicate != null)
			{
				return explicitPredicate;
			}
			
			return this.skip.isEmpty()
				? null /* important for performance optimization */
				: TraversalPredicateSkip.New(
					this.skip.customPredicate()          ,
					this.skip.types().immure()           ,
					this.skip.typesPolymorphic().immure()
				)
			;
		}
				
		protected synchronized TraversalPredicateFull providePredicateFull()
		{
			final TraversalPredicateFull explicitPredicate = this.full.predicate();
			if(explicitPredicate != null)
			{
				return explicitPredicate;
			}
			
			return this.full.isEmpty()
				? null /* important for performance optimization */
				: TraversalPredicateFull.New(
					this.full.instances().immure()       ,
					this.full.customPredicate()          ,
					this.full.types().immure()           ,
					this.full.typesPolymorphic().immure()
				)
			;
		}
		
		protected synchronized TraversalPredicateNode providePredicateNode()
		{
			final TraversalPredicateNode explicitPredicate = this.node.predicate();
			if(explicitPredicate != null)
			{
				return explicitPredicate;
			}
			
			return this.node.isEmpty()
				? null /* important for performance optimization */
				: TraversalPredicateNode.New(
					this.node.instances().immure()       ,
					this.node.customPredicate()          ,
					this.node.types().immure()           ,
					this.node.typesPolymorphic().immure()
				)
			;
		}
		
		protected synchronized TraversalPredicateLeaf providePredicateLeaf()
		{
			final TraversalPredicateLeaf explicitPredicate = this.leaf.predicate();
			if(explicitPredicate != null)
			{
				return explicitPredicate;
			}
			
			return this.leaf.isEmpty()
				? null /* important for performance optimization */
				: TraversalPredicateLeaf.New(
					this.leaf.instances().immure()       ,
					this.leaf.customPredicate()          ,
					this.leaf.types().immure()           ,
					this.leaf.typesPolymorphic().immure()
				)
			;
		}
		
		protected synchronized TraversalMode provideTraversalMode()
		{
			return this.traversalMode != null
				? this.traversalMode
				: new TraversalMode.Full()
			;
		}
		
		@Override
		public synchronized ObjectGraphTraverser buildObjectGraphTraverser()
		{
			return ObjectGraphTraverser.New(
				this.roots()                          ,
				this.provideSkippedInstances()        ,
				this.provideAlreadyHandledProvider()  ,
				this.provideReferenceHandlerProvider(),
				this.provideTypeTraverserProvider()   ,
				this.providePredicateSkip()           ,
				this.providePredicateNode()           ,
				this.providePredicateLeaf()           ,
				this.providePredicateFull()           ,
				this.predicateHandle()                ,
				this.provideAcceptor()                ,
				this.provideMutator()                 ,
				this.provideMutationListener()        ,
				this.provideTraversalMode()           ,
				this.initializerLogic()               ,
				this.finalizerLogic()
			);
		}
							
	}
	
}
