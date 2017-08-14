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
import java.nio.file.Path;
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
	
	
	
	
	public static XGettingSet<Class<?>> defaultLeafTypes()
	{
		/*
		 * Types that are actually value types with unshared references as a storage (usually an array).
		 *
		 * System types that are hardly ever desired to be iterated further. If they should be, a custom handler
		 * can be registered to override the default.
		 *
		 * This list is provisional and definitely not complete. Missing types can be added explicitely via leafType().
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

			BigInteger.class   ,
			BigDecimal.class   ,

			Date.class         ,
			File.class         ,

			Instant.class      ,
			Path.class         ,

			String.class       ,
			StringBuilder.class,
			StringBuffer.class ,

			Thread.class       ,
			OutputStream.class ,
			InputStream.class  ,
			FileChannel.class  ,
			ByteBuffer.class   ,
			Throwable.class    ,

			Class.class        ,
			Field.class        ,
			Method.class
		);
	}
	
	public static XGettingTable<Class<?>, TypeTraverser<?>> defaultPolymorphTypeTraversers()
	{
		return ConstHashTable.<Class<?>, TypeTraverser<?>>New(
			keyValue(XReplacingBag     .class, new TraverserXCollectionMutable()),
			keyValue(XGettingCollection.class, new TraverserXCollectionMutable()),
			keyValue(Collection        .class, new TraverserCollectionOld()     ),
			keyValue(Object[]          .class, new TraverserArray()             )
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
		private final HashEnum<Class<?>>                           skippedTypes              ;
		private final HashEnum<Class<?>>                           skippedTypesPolymorphic   ;
		
		
		private final HashEnum<Class<?>>                           leafTypes                 ;
		private final HashTable<Object, TypeTraverser<?>>          traversersPerInstance     ;
		private final HashTable<Class<?>, TypeTraverser<?>>        traversersPerConcreteType ;
		private final HashTable<Class<?>, TypeTraverser<?>>        traversersPerPolymorphType;
		
		private Predicate<? super Field>                           traversableFieldSelector;
		private Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ;
		private TypeTraverser.Creator                              typeTraverserCreator    ;
		
		private TraversalAcceptor                 acceptor                ;
		private TraversalMutator                  mutator                 ;
		private Object[]                          roots                   ;
		private TraversalReferenceHandlerProvider referenceHandlerProvider;
		private Predicate<Object>                 handlingPredicate       ;
		private Predicate<Object>                 logicPredicate          ;
		private Consumer<Object>                  acceptorLogic           ;
		private Function<Object, Object>          mutatorLogic            ;
		private MutationListener.Provider         mutationListenerProvider;
		private MutationListener                  mutationListener        ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
			this.skipped                  = HashEnum.New() ;
			this.skippedTypes             = HashEnum.New() ;
			this.skippedTypesPolymorphic  = HashEnum.New() ;
			this.traversersPerInstance    = HashTable.New();
			
			this.leafTypes                = HashEnum.New(
				ObjectGraphTraverserBuilder.defaultLeafTypes())
			;
			this.traversersPerPolymorphType = HashTable.New(
				ObjectGraphTraverserBuilder.defaultPolymorphTypeTraversers())
			;
			this.traversersPerConcreteType  = HashTable.New(
				ObjectGraphTraverserBuilder.defaultConcreteTypeTraversers()
			);
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
		
		protected synchronized MutationListener.Provider provideMutationListenerProvider()
		{
			if(this.mutationListenerProvider != null)
			{
				return this.mutationListenerProvider;
			}
			
			if(this.mutationListener != null)
			{
				return MutationListener.Provider(this.mutationListener);
			}
			return null;
		}
		
		public synchronized ObjectGraphTraverserBuilder setMutationListener(
			final MutationListener mutationListener
		)
		{
			this.mutationListener = mutationListener;
			return this;
		}
		
		public synchronized ObjectGraphTraverserBuilder setMutationListenerProvider(
			final MutationListener.Provider mutationListenerProvider
		)
		{
			this.mutationListenerProvider = mutationListenerProvider;
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
		
		@Override
		public synchronized ObjectGraphTraverser buildObjectGraphTraverser()
		{
			return ObjectGraphTraverser.New(
				this.internalGetRoots()               ,
				this.provideSkipped()                 ,
				this.provideSkippedTypes()            ,
				this.provideSkippedTypesPolymorphic() ,
				this.provideAlreadyHandledProvider()  ,
				this.provideReferenceHandlerProvider(),
				this.provideTypeTraverserProvider()   ,
				this.handlingPredicate()              ,
				this.provideAcceptor()                ,
				this.provideMutator()                 ,
				this.provideMutationListenerProvider()
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
		
	}
	
}
