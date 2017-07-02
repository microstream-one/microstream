package net.jadoth.traversal2;

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

public interface ObjectGraphTraverser2Factory
{
	public ObjectGraphTraverser2 buildObjectGraphTraverser();
	
	public default boolean skip(final Object instance)
	{
		synchronized(this)
		{
			return this.skipped().add(instance);
		}
	}

	public default ObjectGraphTraverser2Factory skipAll(final Object... instances)
	{
		synchronized(this)
		{
			this.skipped().addAll(instances);
		}
		return this;
	}
	
	public default ObjectGraphTraverser2Factory skipAll(final Iterable<?> instances)
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

	public default ObjectGraphTraverser2Factory leafTypes(final Class<?>... leafTypes)
	{
		synchronized(this)
		{
			this.leafTypes().addAll(leafTypes);
		}
		return this;
	}
	

	public default ObjectGraphTraverser2Factory leafTypes(final Iterable<Class<?>> types)
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
	
	public ObjectGraphTraverser2Factory setTraversableFieldSelector(
		Predicate<? super Field> traversableFieldSelector
	);
	
	public ObjectGraphTraverser2Factory setTraversalAcceptor(TraversalAcceptor acceptor);
	
	public ObjectGraphTraverser2Factory setTraversalMutator(TraversalMutator mutator);
	
	public default ObjectGraphTraverser2Factory traverse(final Object root)
	{
		return this.traverseAll(root);
	}
	
	public ObjectGraphTraverser2Factory traverseAll(Object... root);
	
	public ObjectGraphTraverser2Factory where(Predicate<Object> predicate);
	
	public ObjectGraphTraverser2Factory apply(Consumer<Object> logic);
	
	public ObjectGraphTraverser2Factory mutateBy(Function<Object, Object> logic);
	
	public XSet<Object> skipped();
	
	public XMap<Object, TraverserAccepting<?>> handlersPerInstance();
	
	public XSet<Class<?>> leafTypes();
	
	public XMap<Class<?>, TraverserAccepting<?>> handlersPerConcreteType();
	
	public XTable<Class<?>, TraverserAccepting<?>> handlersPerPolymorphType();
	
	
	
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
	
	public static XGettingTable<Class<?>, TraverserAccepting<?>> defaultPolymorphTypeTraversalHandlers()
	{
		return ConstHashTable.<Class<?>, TraverserAccepting<?>>New(
			keyValue(XReplacingBag     .class, new TraverserXCollectionMutable()),
			keyValue(XGettingCollection.class, new TraverserXCollectionMutable()),
			keyValue(Collection        .class, new TraverserCollectionOld()     ),
			keyValue(Object[]          .class, new TraverserArray()             )
		);
	}
	
	public static XGettingTable<Class<?>, TraverserAccepting<?>> defaultConcreteTypeTraversalHandlers()
	{
		return ConstHashTable.<Class<?>, TraverserAccepting<?>>New(
			/* empty so far */
		);
	}
	
	
	
	public static ObjectGraphTraverser2Factory New()
	{
		return new ObjectGraphTraverser2Factory.Implementation();
	}
		
	public final class Implementation implements ObjectGraphTraverser2Factory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final HashEnum<Object>                         skipped                 ;
		private final HashEnum<Class<?>>                       leafTypes               ;
		private final HashTable<Object, TraverserAccepting<?>>   handlersPerInstance     ;
		private final HashTable<Class<?>, TraverserAccepting<?>> handlersPerConcreteType ;
		private final HashTable<Class<?>, TraverserAccepting<?>> handlersPerPolymorphType;
		
		private Predicate<? super Field>                           traversableFieldSelector;
		private Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ;
		private TraverserAcceptingCreator                            traversalHandlerCreator ;
		
		private TraversalAcceptor        acceptor     ;
		private TraversalMutator         mutator      ;
		private Object[]                 root         ;
		private Predicate<Object>        predicate    ;
		private Consumer<Object>         acceptorLogic;
		private Function<Object, Object> mutatorLogic ;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
			this.skipped                  = HashEnum.New() ;
			this.handlersPerInstance      = HashTable.New();
			
			this.leafTypes                = HashEnum.New(
				ObjectGraphTraverser2Factory.defaultLeafTypes())
			;
			this.handlersPerPolymorphType = HashTable.New(
				ObjectGraphTraverser2Factory.defaultPolymorphTypeTraversalHandlers())
			;
			this.handlersPerConcreteType  = HashTable.New(
				ObjectGraphTraverser2Factory.defaultConcreteTypeTraversalHandlers()
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized ObjectGraphTraverser2Factory setTraversableFieldSelector(final Predicate<? super Field> traversableFieldSelector)
		{
			this.traversableFieldSelector = traversableFieldSelector;
			return this;
		}

		@Override
		public XSet<Object> skipped()
		{
			return this.skipped;
		}

		@Override
		public XMap<Object, TraverserAccepting<?>> handlersPerInstance()
		{
			return this.handlersPerInstance;
		}

		@Override
		public XSet<Class<?>> leafTypes()
		{
			return this.leafTypes;
		}

		@Override
		public XMap<Class<?>, TraverserAccepting<?>> handlersPerConcreteType()
		{
			return this.handlersPerConcreteType;
		}

		@Override
		public XTable<Class<?>, TraverserAccepting<?>> handlersPerPolymorphType()
		{
			return this.handlersPerPolymorphType;
		}
		
		protected synchronized TraverserAcceptingProvider provideTraversalHandlerProvider()
		{
			return TraverserAcceptingProvider.New(
				this.handlersPerInstance()           ,
				this.handlersPerConcreteType()       ,
				this.handlersPerPolymorphType()      ,
				this.leafTypes()                     ,
				this.provideTraversalHandlerCreator()
			);
		}
		
		protected synchronized XGettingCollection<Object> provideSkipped()
		{
			return this.skipped;
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
		public synchronized ObjectGraphTraverser2Factory setTraversalAcceptor(final TraversalAcceptor acceptor)
		{
			this.acceptor = acceptor;
			return this;
		}
		
		protected synchronized TraversalAcceptor provideAcceptor()
		{
			return this.acceptor;
		}
		
		protected synchronized Predicate<? super Field> provideTraversableFieldSelector()
		{
			if(this.traversableFieldSelector == null)
			{
				this.traversableFieldSelector = JadothReflect::isReference;
			}
			
			return this.traversableFieldSelector;
		}
		
		protected synchronized TraverserAcceptingCreator provideTraversalHandlerCreator()
		{
			if(this.traversalHandlerCreator == null)
			{
				this.traversalHandlerCreator = TraverserAcceptingCreator.New(
					this.provideTraversableFieldSelector()
				);
			}
			
			return this.traversalHandlerCreator;
		}
		
		@Override
		public synchronized ObjectGraphTraverser2 buildObjectGraphTraverser()
		{
			return ObjectGraphTraverser2.New(
				this.provideTraversalHandlerProvider(),
				this.provideSkipped()                 ,
				this.provideAlreadyHandledProvider()  ,
				this.provideAcceptor()
			);
		}

		@Override
		public ObjectGraphTraverser2Factory setTraversalMutator(final TraversalMutator mutator)
		{
			this.mutator = mutator;
			return this;
		}

		@Override
		public ObjectGraphTraverser2Factory traverseAll(final Object... root)
		{
			this.root = root;
			return this;
		}

		@Override
		public ObjectGraphTraverser2Factory where(final Predicate<Object> predicate)
		{
			this.predicate = predicate;
			return this;
		}

		@Override
		public ObjectGraphTraverser2Factory apply(final Consumer<Object> logic)
		{
			this.acceptorLogic = logic;
			return this;
		}

		@Override
		public ObjectGraphTraverser2Factory mutateBy(final Function<Object, Object> logic)
		{
			this.mutatorLogic = logic;
			return this;
		}
		
	}
	
}
