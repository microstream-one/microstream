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
	
	// (01.08.2017 TM)FIXME: if there'a an "alreadyhandled" set, there must be a correspondind "handler" mechanism.
	
	
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
		final XSet<Object> skipped = this.skipped();
		synchronized(this)
		{
			for(final Object instance : instances)
			{
				skipped.add(instance);
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
	
	public Consumer<Object> acceptorLogic();
	
	public Function<Object, Object> mutatorLogic();
	
	public Predicate<Object> predicate();
	
	public default ObjectGraphTraverserBuilder from(final Object root)
	{
		return this.roots(root);
	}
	
	public ObjectGraphTraverserBuilder roots(Object... root);
	
	public ObjectGraphTraverserBuilder select(Predicate<Object> predicate);
	
	public ObjectGraphTraverserBuilder apply(Consumer<Object> logic);
	
	public ObjectGraphTraverserBuilder mutateBy(Function<Object, Object> logic);
	
	public XSet<Object> skipped();
	
	public XMap<Object, TypeTraverser<?>> traversersPerInstance();
	
	public XSet<Class<?>> leafTypes();
	
	public XMap<Class<?>, TypeTraverser<?>> traversersPerConcreteType();
	
	public XTable<Class<?>, TypeTraverser<?>> traversersPerPolymorphType();
	
	public <T> ObjectGraphTraverserBuilder registerTraverserForType(Class<? extends T> type, TypeTraverser<T> traverser);
	
	
	
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

		private final HashEnum<Object>                             skipped                 ;
		private final HashEnum<Class<?>>                           leafTypes               ;
		private final HashTable<Object, TypeTraverser<?>>          traversersPerInstance     ;
		private final HashTable<Class<?>, TypeTraverser<?>>        traversersPerConcreteType ;
		private final HashTable<Class<?>, TypeTraverser<?>>        traversersPerPolymorphType;
		
		private Predicate<? super Field>                           traversableFieldSelector;
		private Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ;
		private TypeTraverser.Creator                              traversalHandlerCreator ;
		
		private TraversalAcceptor         acceptor                ;
		private TraversalMutator          mutator                 ;
		private Object[]                  roots                   ;
		private Predicate<Object>         predicate               ;
		private Consumer<Object>          acceptorLogic           ;
		private Function<Object, Object>  mutatorLogic            ;
		private MutationListener.Provider mutationListenerProvider;
		private MutationListener          mutationListener        ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
			this.skipped                  = HashEnum.New() ;
			this.traversersPerInstance      = HashTable.New();
			
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
		public synchronized Predicate<Object> predicate()
		{
			return this.predicate;
		}

		@Override
		public synchronized XSet<Object> skipped()
		{
			return this.skipped;
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
				this.provideTraverserAcceptingCreator(),
				this.traversersPerInstance()             ,
				this.traversersPerConcreteType()         ,
				this.traversersPerPolymorphType()        ,
				this.leafTypes()
			);
		}
		
		protected synchronized XGettingCollection<Object> provideSkipped()
		{
			return this.skipped;
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
		
		protected synchronized TraversalAcceptor provideAcceptor()
		{
			if(this.acceptor != null)
			{
				return this.acceptor;
			}
			
			if(this.acceptorLogic != null)
			{
				return this.predicate != null
					? TraversalAcceptor.New(this.predicate, this.acceptorLogic)
					: TraversalAcceptor.New(this.acceptorLogic)
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
		
		protected synchronized TypeTraverser.Creator provideTraverserAcceptingCreator()
		{
			if(this.traversalHandlerCreator == null)
			{
				this.traversalHandlerCreator = TraverserReflective.Creator(
					this.provideTraversableFieldSelector()
				);
			}
			
			return this.traversalHandlerCreator;
		}
		
		@Override
		public synchronized ObjectGraphTraverser buildObjectGraphTraverser()
		{
			ObjectGraphTraverser ogt;
			
			ogt = this.buildByTraversalHandlers();
			if(ogt != null)
			{
				return ogt;
			}
			
			ogt = this.buildByLogic();
			if(ogt != null)
			{
				return ogt;
			}
			
			return ObjectGraphTraverser.New(
				this.internalGetRoots()                 ,
				this.provideSkipped()                   ,
				this.provideAlreadyHandledProvider()    ,
				this.provideTypeTraverserProvider(),
				this.provideMutationListenerProvider()
			);
		}
		
		protected synchronized ObjectGraphTraverser buildByTraversalHandlers()
		{
			final TraversalMutator mutator = this.mutator();
			if(mutator != null)
			{
				return ObjectGraphTraverser.New(
					this.internalGetRoots()                 ,
					this.provideSkipped()                   ,
					this.provideAlreadyHandledProvider()    ,
					this.provideTypeTraverserProvider(),
					this.provideMutationListenerProvider()  ,
					mutator
				);
			}

			final TraversalAcceptor acceptor = this.acceptor();
			if(acceptor != null)
			{
				return ObjectGraphTraverser.New(
					this.internalGetRoots()                 ,
					this.provideSkipped()                   ,
					this.provideAlreadyHandledProvider()    ,
					this.provideTypeTraverserProvider(),
					this.provideMutationListenerProvider()  ,
					acceptor
				);
			}
			
			return null;
		}
		
		protected synchronized ObjectGraphTraverser buildByLogic()
		{
			final Function<Object, Object> mutatorLogic = this.mutatorLogic();
			if(mutatorLogic != null)
			{
				final Predicate<Object> predicate = this.predicate();
				return ObjectGraphTraverser.New(
					this.internalGetRoots()                 ,
					this.provideSkipped()                   ,
					this.provideAlreadyHandledProvider()    ,
					this.provideTypeTraverserProvider(),
					this.provideMutationListenerProvider()  ,
					predicate == null
						? TraversalMutator.New(mutatorLogic)
						: TraversalMutator.New(predicate, mutatorLogic)
				);
			}
			
			final Consumer<Object> acceptorLogic = this.acceptorLogic();
			if(acceptorLogic != null)
			{
				final Predicate<Object> predicate = this.predicate();
				return ObjectGraphTraverser.New(
					this.internalGetRoots()                 ,
					this.provideSkipped()                   ,
					this.provideAlreadyHandledProvider()    ,
					this.provideTypeTraverserProvider(),
					this.provideMutationListenerProvider()  ,
					predicate == null
					? TraversalAcceptor.New(acceptorLogic)
					: TraversalAcceptor.New(predicate, acceptorLogic)
				);
			}
			
			return null;
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
			this.predicate = predicate;
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
