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
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingSet;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XMap;
import net.jadoth.collections.types.XReplacingBag;
import net.jadoth.collections.types.XSequence;
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
	
	public default boolean skip(final Object instance)
	{
		synchronized(this)
		{
			return this.skipped().add(instance);
		}
	}

	public default ObjectGraphTraverserBuilder skipAll(final Object... instances)
	{
		synchronized(this)
		{
			this.skipped().addAll(instances);
		}
		return this;
	}
	
	public default ObjectGraphTraverserBuilder skipAll(final Iterable<?> instances)
	{
		synchronized(this)
		{
			final XSet<Object> skipped = this.skipped();
			for(final Object instance : instances)
			{
				skipped.add(instance);
			}
		}
		
		return this;
	}
	
	public default boolean skipType(final Class<?> type)
	{
		synchronized(this)
		{
			return this.skippedTypes().add(type);
		}
	}

	public default ObjectGraphTraverserBuilder skipTypes(final Class<?>... types)
	{
		synchronized(this)
		{
			this.skippedTypes().addAll(types);
		}
		return this;
	}
	
	public default ObjectGraphTraverserBuilder skipTypes(final Iterable<Class<?>> types)
	{
		synchronized(this)
		{
			final XSet<Class<?>> skippedTypes = this.skippedTypes();
			for(final Class<?> type : types)
			{
				skippedTypes.add(type);
			}
		}
		
		return this;
	}
	
	public default boolean skipTypePolymorphic(final Class<?> type)
	{
		synchronized(this)
		{
			return this.skippedTypesPolymorphic().add(type);
		}
	}

	public default ObjectGraphTraverserBuilder skipTypesPolymorphic(final Class<?>... types)
	{
		synchronized(this)
		{
			this.skippedTypesPolymorphic().addAll(types);
		}
		return this;
	}
	
	public default ObjectGraphTraverserBuilder skipTypesPolymorphic(final Iterable<Class<?>> types)
	{
		synchronized(this)
		{
			final XSequence<Class<?>> skippedTypesPolymorphic = this.skippedTypesPolymorphic();
			for(final Class<?> type : types)
			{
				skippedTypesPolymorphic.add(type);
			}
		}
		
		return this;
	}


	public default boolean leafType(final Class<?> leafType)
	{
		synchronized(this)
		{
			return this.leafTypes().add(leafType);
		}
	}

	public default ObjectGraphTraverserBuilder leafTypes(final Class<?>... leafTypes)
	{
		synchronized(this)
		{
			this.leafTypes().addAll(leafTypes);
		}
		return this;
	}
	

	public default ObjectGraphTraverserBuilder leafTypes(final Iterable<Class<?>> types)
	{
		final XSet<Class<?>> leafTypes = this.leafTypes();
		synchronized(this)
		{
			for(final Class<?> type : leafTypes)
			{
				leafTypes.add(type);
			}
		}
		
		return this;
	}
	
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
	
	public TraversalMode traversalMode();
		
	
	public TraversalAcceptor acceptor();
	
	public TraversalMutator mutator();
	
	public Predicate<Object> handlingPredicate();
	
	public Consumer<Object> acceptorLogic();
	
	public Function<Object, Object> mutatorLogic();
	
	public Predicate<Object> logicPredicate();
	
	public default ObjectGraphTraverserBuilder from(final Object root)
	{
		return this.roots(root);
	}
	
	public ObjectGraphTraverserBuilder roots(Object... root);
	
	public ObjectGraphTraverserBuilder select(Predicate<Object> predicate);
	
	public ObjectGraphTraverserBuilder apply(Consumer<Object> logic);
	
	public ObjectGraphTraverserBuilder mutateBy(Function<Object, Object> logic);
	
	public XSet<Object> skipped();
	
	public XSet<Class<?>> skippedTypes();
	
	public XSequence<Class<?>> skippedTypesPolymorphic();
	
	public XMap<Object, TypeTraverser<?>> traversersPerInstance();
	
	public XSet<Class<?>> leafTypes();
	
	public XMap<Class<?>, TypeTraverser<?>> traversersPerConcreteType();
	
	public XTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType();
	
	public <T> ObjectGraphTraverserBuilder registerTraverserForType(Class<? extends T> type, TypeTraverser<T> traverser);
	
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

		private final HashEnum<Object>                             skipped                   ;
		private       TraversalPredicateSkip                       skipPredicate             ;
		private       Predicate<Object>                            skipCustomPredicate       ;
		private final HashEnum<Class<?>>                           skippedTypes              ;
		private final HashEnum<Class<?>>                           skippedTypesPolymorphic   ;

		private       TraversalPredicateLeaf                       leafPredicate             ;
		private final HashEnum<Object>                             leafInstances             ;
		private       Predicate<Object>                            leafCustomPredicate       ;
		private final HashEnum<Class<?>>                           leafTypes                 ;
		private final HashEnum<Class<?>>                           leafTypesPolymorphic      ;

		private       TraversalPredicateNode                       nodePredicate             ;
		private final HashEnum<Object>                             nodeInstances             ;
		private       Predicate<Object>                            nodeCustomPredicate       ;
		private final HashEnum<Class<?>>                           nodeTypes                 ;
		private final HashEnum<Class<?>>                           nodeTypesPolymorphic      ;

		private       TraversalPredicateFull                       fullPredicate             ;
		private final HashEnum<Object>                             fullInstances             ;
		private       Predicate<Object>                            fullCustomPredicate       ;
		private final HashEnum<Class<?>>                           fullTypes                 ;
		private final HashEnum<Class<?>>                           fullTypesPolymorphic      ;
		
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
			this.skipped                  = HashEnum.New();
			this.skippedTypes             = HashEnum.New(ObjectGraphTraverserBuilder.defaultSkipTypesConcrete());
			this.skippedTypesPolymorphic  = HashEnum.New(ObjectGraphTraverserBuilder.defaultSkipTypesPolymorphic());
			this.leafInstances            = HashEnum.New();
			this.leafTypes                = HashEnum.New();
			this.leafTypesPolymorphic     = HashEnum.New();
			this.nodeInstances            = HashEnum.New();
			this.nodeTypes                = HashEnum.New();
			this.nodeTypesPolymorphic     = HashEnum.New();
			this.fullInstances            = HashEnum.New();
			this.fullTypes                = HashEnum.New();
			this.fullTypesPolymorphic     = HashEnum.New();
			
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
		public synchronized XSet<Object> skipped()
		{
			return this.skipped;
		}
		
		@Override
		public synchronized XSet<Class<?>> skippedTypes()
		{
			return this.skippedTypes;
		}
		
		@Override
		public synchronized XSequence<Class<?>> skippedTypesPolymorphic()
		{
			return this.skippedTypesPolymorphic;
		}

		@Override
		public synchronized XMap<Object, TypeTraverser<?>> traversersPerInstance()
		{
			return this.traversersPerInstance;
		}

		@Override
		public synchronized XSet<Class<?>> leafTypes()
		{
			return this.leafTypes;
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
