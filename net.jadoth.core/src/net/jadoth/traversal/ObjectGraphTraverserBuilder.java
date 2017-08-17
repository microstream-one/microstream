package net.jadoth.traversal;

import static net.jadoth.Jadoth.keyValue;

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

import net.jadoth.collections.ConstHashEnum;
import net.jadoth.collections.ConstHashTable;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XMap;
import net.jadoth.collections.types.XReplacingBag;
import net.jadoth.collections.types.XSet;
import net.jadoth.collections.types.XTable;
import net.jadoth.reflect.JadothReflect;

public interface ObjectGraphTraverserBuilder
{
	/* (21.07.2017 TM)TODO: what about a traversal predicate and exclude types?
	 * see:
//			.setTraversableFieldSelector((c, f) ->
//				!JadothReflect.isTransient(f)
//			)
//			.excludeTypes(
//				Unpersistable.class,
//				BinaryLeafType.class
//			)
	 */
	
	
	public ObjectGraphTraverser buildObjectGraphTraverser();
	
	public TraversalFilter<TraversalPredicateSkip> skip();
	
	public TraversalFilter<TraversalPredicateNode> node();
	
	public TraversalFilter<TraversalPredicateLeaf> leaf();
	
	public TraversalFilter<TraversalPredicateFull> full();



		
	public ObjectGraphTraverserBuilder setTraversableFieldSelector(
		Predicate<? super Field> traversableFieldSelector
	);
	
	public ObjectGraphTraverserBuilder setTraversalAcceptor(TraversalAcceptor acceptor);
	
	public ObjectGraphTraverserBuilder setTraversalMutator(TraversalMutator mutator);
	
	public ObjectGraphTraverserBuilder setTraversalMode(TraversalMode traversalMode);
	
	public default ObjectGraphTraverserBuilder setModeNode()
	{
		// tiny instantiation instead of permanent memory consumption by a constant.
		return this.setTraversalMode(new TraversalMode.Node());
	}
	
	public default ObjectGraphTraverserBuilder setModeFull()
	{
		// tiny instantiation instead of permanent memory consumption by a constant.
		return this.setTraversalMode(new TraversalMode.Full());
	}
	
	public default ObjectGraphTraverserBuilder setModeLeaf()
	{
		// tiny instantiation instead of permanent memory consumption by a constant.
		return this.setTraversalMode(new TraversalMode.Leaf());
	}
	
	
	
	
	

	public ObjectGraphTraverserBuilder acceptor(TraversalAcceptor acceptor);
	
	public default ObjectGraphTraverserBuilder apply(final Consumer<Object> logic)
	{
		return this.apply((Predicate<Object>)null, logic);
	}
	
	public ObjectGraphTraverserBuilder apply(Predicate<Object> predicate, Consumer<Object> logic);
	
	public <T> ObjectGraphTraverserBuilder apply(Class<T> predicate, Consumer<? super T> logic);
	
	
	
	public ObjectGraphTraverserBuilder mutator(TraversalMutator mutator);
	
	public default ObjectGraphTraverserBuilder mutate(final Function<Object, Object> logic)
	{
		return this.mutate((Predicate<Object>)null, logic);
	}
	
	public ObjectGraphTraverserBuilder mutate(Predicate<Object> predicate, Function<Object, ?> logic);
	
	public <T> ObjectGraphTraverserBuilder mutate(Class<T> predicate, Function<? super T, ?> logic);
	
	
	
	
	public TraversalAcceptor acceptor();
	
	public Consumer<Object> acceptorLogic();
	
	public Predicate<Object> acceptorPredicate();
	

	public TraversalMutator mutator();
	
	public Function<Object, ?> mutatorLogic();
	
	public Predicate<Object> mutatorPredicate();
	
	
	
	public default ObjectGraphTraverserBuilder root(final Object root)
	{
		return this.roots(root);
	}
	
	public ObjectGraphTraverserBuilder roots(Object... root);
	

	public TraversalMode traversalMode();
	
		
	

	
	
	public XMap<Object, TypeTraverser<?>> traversersPerInstance();
		
	public XMap<Class<?>, TypeTraverser<?>> traversersPerConcreteType();
	
	public XTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType();
	
	public <T> ObjectGraphTraverserBuilder registerTraverserForType(Class<? extends T> type, TypeTraverser<T> traverser);
	
	public <T> ObjectGraphTraverserBuilder registerTraverserForTypePolymorphic(Class<? extends T> type, TypeTraverser<T> traverser);
	
	public TraversalReferenceHandlerProvider provideReferenceHandlerProvider();
	
	public TraversalAcceptor provideAcceptor();
	
	public TraversalMutator provideMutator();
	
	
	
	
	public static XGettingSet<Class<?>> defaultSkipTypesConcrete()
	{
		/*
		 * Types that are actually value types with no or unshared references as a storage (usually an array).
		 *
		 * System types that are hardly ever desired to be iterated further. If they have to be, they can be
		 * counter-registered as a full, node or leaf type.
		 *
		 * This list is provisional and definitely not complete. Missing types can be added explicitely in the builder.
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
		 * This list is provisional and definitely not complete. Missing types can be added explicitely in the builder.
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
			keyValue(XReplacingBag     .class, new TraverserXCollectionMutable()  ),
			keyValue(XGettingCollection.class, new TraverserXCollectionImmutable()),
			keyValue(Collection        .class, new TraverserCollectionOld()       ),
			keyValue(Object[]          .class, new TraverserArray()               )
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
		return new ObjectGraphTraverserBuilder.Implementation();
	}
		
	public final class Implementation implements ObjectGraphTraverserBuilder
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
		
		private Predicate<? super Field>                           traversableFieldSelector  ;
		private Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider    ;
		private TypeTraverser.Creator                              typeTraverserCreator      ;
                                                                                             
		private TraversalAcceptor                                  acceptor                  ;
		private Predicate<Object>                                  acceptorPredicate         ;
		private Consumer<Object>                                   acceptorLogic             ;
                                                                                             
		private TraversalMutator                                   mutator                   ;
		private Predicate<Object>                                  mutatorPredicate          ;
		private Function<Object, Object>                           mutatorLogic              ;
		                                                                                     
		private MutationListener                                   mutationListener          ;
		private TraversalMode                                      traversalMode             ;
		private TraversalReferenceHandlerProvider                  referenceHandlerProvider  ;
		private Object[]                                           roots                     ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
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
		public TraversalFilter<TraversalPredicateSkip> skip()
		{
			return this.skip;
		}
		
		@Override
		public TraversalFilter<TraversalPredicateNode> node()
		{
			return this.node;
		}
		
		@Override
		public TraversalFilter<TraversalPredicateLeaf> leaf()
		{
			return this.leaf;
		}
		
		@Override
		public TraversalFilter<TraversalPredicateFull> full()
		{
			return this.full;
		}

		@Override
		public synchronized ObjectGraphTraverserBuilder setTraversableFieldSelector(final Predicate<? super Field> traversableFieldSelector)
		{
			this.traversableFieldSelector = traversableFieldSelector;
			return this;
		}
		
		@Override
		public synchronized TraversalAcceptor acceptor()
		{
			return this.acceptor;
		}
		
		@Override
		public synchronized TraversalMutator mutator()
		{
			return this.mutator;
		}
		
		@Override
		public synchronized Consumer<Object> acceptorLogic()
		{
			return this.acceptorLogic;
		}
		
		@Override
		public synchronized Function<Object, Object> mutatorLogic()
		{
			return this.mutatorLogic;
		}
		
		@Override
		public synchronized Predicate<Object> handlingPredicate()
		{
			return this.handlingPredicate;
		}
		
		@Override
		public synchronized Predicate<Object> logicPredicate()
		{
			return this.logicPredicate;
		}

		@Override
		public synchronized XMap<Object, TypeTraverser<?>> traversersPerInstance()
		{
			return this.traversersPerInstance;
		}

		@Override
		public synchronized XMap<Class<?>, TypeTraverser<?>> traversersPerConcreteType()
		{
			return this.traversersPerConcreteType;
		}

		@Override
		public synchronized XTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType()
		{
			return this.traversersPerPolymorphType;
		}
		
		protected synchronized TypeTraverserProvider provideTypeTraverserProvider()
		{
			return TypeTraverserProvider.New(
				this.provideTypeTraverserCreator(),
				this.traversersPerInstance()             ,
				this.traversersPerConcreteType()         ,
				this.traversersPerPolymorphType()        ,
				this.leafTypes()
			);
		}
		
		protected synchronized XGettingCollection<Object> provideSkipped()
		{
			return this.skipped();
		}
		
		protected synchronized XGettingSet<Class<?>> provideSkippedTypes()
		{
			return this.skippedTypes();
		}
		
		protected synchronized XGettingSequence<Class<?>> provideSkippedTypesPolymorphic()
		{
			return this.skippedTypesPolymorphic();
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
		public synchronized ObjectGraphTraverserBuilder setTraversalAcceptor(final TraversalAcceptor acceptor)
		{
			this.acceptor = acceptor;
			return this;
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
				return this.logicPredicate != null
					? TraversalAcceptor.New(this.logicPredicate, this.acceptorLogic)
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
				return this.logicPredicate != null
					? TraversalMutator.New(this.logicPredicate, this.mutatorLogic)
					: TraversalMutator.New(this.mutatorLogic)
				;
			}
			
			return null;
		}
		
		protected synchronized Object[] internalGetRoots()
		{
			return this.roots;
		}
		
		protected synchronized Predicate<? super Field> provideTraversableFieldSelector()
		{
			if(this.traversableFieldSelector == null)
			{
				this.traversableFieldSelector = JadothReflect::isReference;
			}
			
			return this.traversableFieldSelector;
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
			return this.referenceHandlerProvider;
		}
		
		protected synchronized TraversalPredicateSkip providePredicateSkip()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectGraphTraverserBuilder.Implementation#providePredicateSkip()
		}
				
		protected synchronized TraversalPredicateFull providePredicateFull()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectGraphTraverserBuilder.Implementation#providePredicateIsFull()
		}
		
		protected synchronized TraversalPredicateNode providePredicateNode()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectGraphTraverserBuilder.Implementation#providePredicateIsNode()
		}
		
		protected synchronized TraversalPredicateLeaf providePredicateLeaf()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectGraphTraverserBuilder.Implementation#providePredicateIsLeaf()
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
				this.internalGetRoots()               ,
				this.provideSkipped()                 ,
				this.provideAlreadyHandledProvider()  ,
				this.provideReferenceHandlerProvider(),
				this.provideTypeTraverserProvider()   ,
				this.providePredicateSkip()           ,
				this.providePredicateNode()           ,
				this.providePredicateLeaf()           ,
				this.providePredicateFull()           ,
				this.provideAcceptor()                ,
				this.provideMutator()                 ,
				this.provideMutationListener()        ,
				this.provideTraversalMode()
			);
		}
		
		@Override
		public ObjectGraphTraverserBuilder setTraversalMutator(final TraversalMutator mutator)
		{
			this.mutator = mutator;
			return this;
		}

		@Override
		public ObjectGraphTraverserBuilder roots(final Object... roots)
		{
			this.roots = roots;
			return this;
		}

		@Override
		public ObjectGraphTraverserBuilder select(final Predicate<Object> predicate)
		{
			this.logicPredicate = predicate;
			return this;
		}

		@Override
		public ObjectGraphTraverserBuilder apply(final Consumer<Object> logic)
		{
			this.acceptorLogic = logic;
			return this;
		}

		@Override
		public ObjectGraphTraverserBuilder mutateBy(final Function<Object, Object> logic)
		{
			this.mutatorLogic = logic;
			return this;
		}
		
		@Override
		public synchronized <T> ObjectGraphTraverserBuilder registerTraverserForType(
			final Class<? extends T> type     ,
			final TypeTraverser<T>   traverser
		)
		{
			this.traversersPerConcreteType.add(type, traverser);
			return this;
		}
		
		@Override
		public synchronized ObjectGraphTraverserBuilder setTraversalMode(final TraversalMode traversalMode)
		{
			this.traversalMode = traversalMode;
			return this;
		}
		
		@Override
		public synchronized TraversalMode traversalMode()
		{
			return this.traversalMode;
		}
				
	}
	
}
