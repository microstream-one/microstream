package net.jadoth.traversal;

import static net.jadoth.Jadoth.notNull;

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
import java.util.Date;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.collections.ConstHashEnum;
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSet;
import net.jadoth.collections.types.XSet;
import net.jadoth.functional.JadothPredicates;
import net.jadoth.traversal.handlers.TraversalHandlerArray;
import net.jadoth.traversal.handlers.TraversalHandlerIterable;
import net.jadoth.traversal.handlers.TraversalHandlerMap;
import net.jadoth.traversal.handlers.TraversalHandlerXGettingMap;
import net.jadoth.traversal.handlers.TraversalHandlerXIterable;
import net.jadoth.traversal2.OpenAdressingMiniSet;
import net.jadoth.traversal.TraversalHandlerProvider;
import net.jadoth.util.KeyValue;

public interface ObjectGraphTraverserFactory
{
	public static XGettingSet<Class<?>> defaultLeafTypes()
	{
		/*
		 * Types that are actually value types with unshared references as a storage (an array in general).
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

	public static XGettingEnum<TraversalHandlerCustomProvider<?>> defaultHandlerProviders()
	{
		/*
		 * Order is crucial here: XGettingMap is also a XIterable, but it is more specific,
		 * so it has to be checked first.
		 * Object[] can never cause such problems, but is checked first for performance reasons.
		 */
		return ConstHashEnum.New(
			new TraversalHandlerArray.Provider()      ,
			new TraversalHandlerXGettingMap.Provider(),
			new TraversalHandlerXIterable.Provider()  ,
			new TraversalHandlerMap.Provider()        ,
			new TraversalHandlerIterable.Provider()
		);
	}

	public ObjectGraphTraverserFactory setTraversableFieldSelector(
		BiPredicate<Class<?>, ? super Field> traversableFieldSelector
	);

	public ObjectGraphTraverserFactory setHandlingLogicProvider(
		TraversalHandlingLogicProvider handlingLogicProvider
	);

	public ObjectGraphTraverserFactory setHandlingLogicProviderProvider(
		TraversalHandlingLogicProviderProvider handlingLogicProviderProvider
	);

	public ObjectGraphTraverserFactory setTraversalHandlerProvider(
		TraversalHandlerProvider traversalHandlerProvider
	);

	public ObjectGraphTraverserFactory setHandlingLogic(
		Predicate<Object> handlingLogic
	);

	public ObjectGraphTraverserFactory setAlreadyHandledProvider(
		Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider
	);


	public boolean skip(Object instance);

	public ObjectGraphTraverserFactory skipAll(Object... instances);

	public ObjectGraphTraverserFactory skipAll(Iterable<?> instances);


	public boolean leafType(Class<?> leafType);

	public ObjectGraphTraverserFactory leafTypes(Class<?>... leafTypes);

	public ObjectGraphTraverserFactory leafTypes(Iterable<Class<?>> leafTypes);


	public boolean nodeType(Class<?> nodeType);

	public ObjectGraphTraverserFactory nodeTypes(Class<?>... nodeTypes);

	public ObjectGraphTraverserFactory nodeTypes(Iterable<Class<?>> nodeTypes);


	public default boolean excludeType(final Class<?> nodeType)
	{
		return this.leafType(nodeType)
			|| this.nodeType(nodeType)
		;
	}

	public default ObjectGraphTraverserFactory excludeTypes(final Class<?>... nodeTypes)
	{
		this.leafTypes(nodeTypes);
		this.nodeTypes(nodeTypes);
		return this;
	}

	public default ObjectGraphTraverserFactory excludeTypes(final Iterable<Class<?>> nodeTypes)
	{
		this.leafTypes(nodeTypes);
		this.nodeTypes(nodeTypes);
		return this;
	}


	public <T> boolean registerTraversalHandlerProviderByType(
		Class<T>                                  type           ,
		TraversalHandlerCustomProvider<? super T> handlerProvider
	);

	public ObjectGraphTraverserFactory registerTraversalHandlerProvidersByType(
		Iterable<? extends KeyValue<Class<?>, ? extends TraversalHandlerCustomProvider<?>>> handlerProviders
	);

	public <T> boolean registerTraversalHandlerProvider(
		TraversalHandlerCustomProvider<T> handlerProvider
	);

	public ObjectGraphTraverserFactory registerTraversalHandlerProviders(
		Iterable<? extends TraversalHandlerCustomProvider<?>> handlerProviders
	);

	public ObjectGraphTraverserFactory registerTraversalHandlerProviders(
		TraversalHandlerCustomProvider<?>... handlerProviders
	);


	public ObjectGraphTraverser buildObjectGraphTraverser();



	public class Implementation implements ObjectGraphTraverserFactory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final HashEnum<Object>                                       skipped          = HashEnum.New() ;
		private final HashTable<Class<?>, TraversalHandlerCustomProvider<?>> handlerProviders = HashTable.New();
		private final HashEnum<Class<?>>                                     leafTypes        = HashEnum.New() ;
		private final HashEnum<Class<?>>                                     nodeTypes        = HashEnum.New() ;

		private Predicate<Object>                                  logic                        ;
		private TraversalHandlingLogicProvider                     handlingLogicProvider        ;
		private TraversalHandlingLogicProviderProvider             handlingLogicProviderProvider;
		private BiPredicate<Class<?>, ? super Field>               traversableFieldSelector     ;
		private TraversalHandlerProvider                           instanceHandlerProvider      ;
		private Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider       ;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected synchronized BiPredicate<Class<?>, ? super Field> provideTraversableFieldSelector()
		{
			return this.traversableFieldSelector;
		}

		protected synchronized XGettingCollection<Object> provideSkipped()
		{
			return this.skipped;
		}

		protected synchronized XGettingCollection<Class<?>> provideLeafTypes()
		{
			return this.leafTypes;
		}

		protected synchronized Function<XGettingCollection<Object>, XSet<Object>>  provideAlreadyHandledProvider()
		{
			if(this.alreadyHandledProvider != null)
			{
				return this.alreadyHandledProvider;
			}

			return OpenAdressingMiniSet::New;
		}

		protected synchronized Predicate<Object> provideLogic()
		{
			if(this.logic != null)
			{
				return this.logic;
			}

			return JadothPredicates.all();
		}

		// yes, yes: funny naming. But logically correct and needed.
		protected synchronized TraversalHandlingLogicProviderProvider provideTraversalHandlingLogicProviderProvider()
		{
			if(this.handlingLogicProviderProvider != null)
			{
				return this.handlingLogicProviderProvider;
			}

			return TraversalHandlingLogicProviderProvider.New(
				this.provideLogic()
			);
		}

		protected synchronized TraversalHandlingLogicProvider provideHandlingLogicProvider()
		{
			// in case a logic provider has been registered explicitely, it is used.
			if(this.handlingLogicProvider != null)
			{
				return this.handlingLogicProvider;
			}

			// otherwise, a provider is created depending on the content of other factory fields
			final TraversalHandlingLogicProviderProvider lpp = this.provideTraversalHandlingLogicProviderProvider();

			return lpp.provideHandlingLogicProvider(this.nodeTypes);
		}

		protected synchronized
		XGettingMap<Class<?>, ? extends TraversalHandlerCustomProvider<?>> provideSpecificTraversalHandlerProviders()
		{
			final TraversalHandlerCustomProvider<Object> leafHandlerProvider = TraversalHandlerCustomProvider
				.NewLeafHandlerProvider()
			;

			// assemble the list of providers in order or descending specificity

			final HashTable<Class<?>, TraversalHandlerCustomProvider<?>> providers = HashTable.New(
				this.handlerProviders
			);

			for(final Class<?> explicitLeafType : this.provideLeafTypes())
			{
				// intentionally add to only supplement the more specific providers, not replace them
				providers.add(explicitLeafType, leafHandlerProvider);
			}

			for(final TraversalHandlerCustomProvider<?> e : ObjectGraphTraverserFactory.defaultHandlerProviders())
			{
				// intentionally add to only supplement the more specific providers, not replace them
				providers.add(notNull(e.handledType()), e);
			}

			for(final Class<?> defaultLeafType : ObjectGraphTraverserFactory.defaultLeafTypes())
			{
				// intentionally add to only supplement the more specific providers, not replace them
				providers.add(defaultLeafType, leafHandlerProvider);
			}

			return providers;
		}

		protected synchronized TraversalHandlerProvider provideTraversalHandlerProvider()
		{
			if(this.instanceHandlerProvider != null)
			{
				return this.instanceHandlerProvider;
			}

			return TraversalHandlerProvider.New(
				this.provideHandlingLogicProvider()            ,
				this.provideTraversableFieldSelector()         ,
				this.provideSpecificTraversalHandlerProviders()
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation setTraversalHandlerProvider(
			final TraversalHandlerProvider instanceHandlerProvider
		)
		{
			this.instanceHandlerProvider = instanceHandlerProvider;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation setAlreadyHandledProvider(
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider
		)
		{
			this.alreadyHandledProvider = alreadyHandledProvider;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation setHandlingLogic(
			final Predicate<Object> handlingLogic
		)
		{
			this.logic = handlingLogic;
			return this;
		}

		@Override
		public synchronized boolean skip(final Object instance)
		{
			return this.skipped.add(instance);
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation skipAll(
			final Object... instances
		)
		{
			final HashEnum<Object> skipped = this.skipped;

			for(final Object object : instances)
			{
				skipped.add(object);
			}

			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation skipAll(
			final Iterable<?> instances
		)
		{
			final HashEnum<Object> skipped = this.skipped;

			for(final Object object : instances)
			{
				skipped.add(object);
			}

			return this;
		}

		@Override
		public synchronized boolean leafType(final Class<?> leafType)
		{
			return this.leafTypes.add(leafType);
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation leafTypes(
			final Class<?>... leafTypes
		)
		{
			final HashEnum<Class<?>> thisLeafTypes = this.leafTypes;

			for(final Class<?> leafType : leafTypes)
			{
				thisLeafTypes.add(leafType);
			}

			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation leafTypes(
			final Iterable<Class<?>> leafTypes
		)
		{
			final HashEnum<Class<?>> thisLeafTypes = this.leafTypes;

			for(final Class<?> leafType : leafTypes)
			{
				thisLeafTypes.add(leafType);
			}

			return this;
		}

		@Override
		public synchronized boolean nodeType(final Class<?> nodeType)
		{
			return this.nodeTypes.add(nodeType);
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation nodeTypes(
			final Class<?>... nodeTypes
		)
		{
			final HashEnum<Class<?>> thisNodeTypes = this.nodeTypes;

			for(final Class<?> nodeType : nodeTypes)
			{
				thisNodeTypes.add(nodeType);
			}

			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation nodeTypes(
			final Iterable<Class<?>> nodeTypes
		)
		{
			final HashEnum<Class<?>> thisNodeTypes = this.nodeTypes;

			for(final Class<?> nodeType : nodeTypes)
			{
				thisNodeTypes.add(nodeType);
			}

			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation setTraversableFieldSelector(
			final BiPredicate<Class<?>, ? super Field> traversableFieldSelector
		)
		{
			this.traversableFieldSelector = traversableFieldSelector;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation setHandlingLogicProvider(
			final TraversalHandlingLogicProvider handlingLogicProvider
		)
		{
			this.handlingLogicProvider = handlingLogicProvider;
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation setHandlingLogicProviderProvider(
			final TraversalHandlingLogicProviderProvider handlingLogicProviderProvider
		)
		{
			this.handlingLogicProviderProvider = handlingLogicProviderProvider;
			return this;
		}

		@Override
		public synchronized <T> boolean registerTraversalHandlerProviderByType(
			final Class<T>                                  type           ,
			final TraversalHandlerCustomProvider<? super T> handlerProvider
		)
		{
			return this.handlerProviders.add(type, handlerProvider);
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation registerTraversalHandlerProvidersByType(
			final Iterable<? extends KeyValue<Class<?>, ? extends TraversalHandlerCustomProvider<?>>> handlerProviders
		)
		{
			final HashTable<Class<?>, TraversalHandlerCustomProvider<?>> specHandlerProvs = this.handlerProviders;
			for(final KeyValue<Class<?>, ? extends TraversalHandlerCustomProvider<?>> entry : handlerProviders)
			{
				specHandlerProvs.add(entry.key(), entry.value());
			}
			return this;
		}

		@Override
		public synchronized <T> boolean registerTraversalHandlerProvider(
			final TraversalHandlerCustomProvider<T> handlerProvider
		)
		{
			final Class<T> handledType = handlerProvider.handledType();
			return this.registerTraversalHandlerProviderByType(notNull(handledType), handlerProvider);
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation registerTraversalHandlerProviders(
			final Iterable<? extends TraversalHandlerCustomProvider<?>> handlerProviders
		)
		{
			// first pass to check that every handleType is not null before registering anything
			for(final TraversalHandlerCustomProvider<?> e : handlerProviders)
			{
				notNull(e.handledType());
			}

			// actual registering pass
			for(final TraversalHandlerCustomProvider<?> e : handlerProviders)
			{
				this.registerTraversalHandlerProvider(e);
			}

			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation registerTraversalHandlerProviders(
			final TraversalHandlerCustomProvider<?>... handlerProviders
		)
		{
			// first pass to check that every handleType is not null before registering anything
			for(final TraversalHandlerCustomProvider<?> e : handlerProviders)
			{
				notNull(e.handledType());
			}

			// actual registering pass
			for(final TraversalHandlerCustomProvider<?> e : handlerProviders)
			{
				this.registerTraversalHandlerProvider(e);
			}

			return this;
		}

		@Override
		public synchronized boolean excludeType(final Class<?> excludedType)
		{
			return ObjectGraphTraverserFactory.super.excludeType(excludedType);
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation excludeTypes(
			final Class<?>... excludedTypes
		)
		{
			ObjectGraphTraverserFactory.super.excludeTypes(excludedTypes);
			return this;
		}

		@Override
		public synchronized ObjectGraphTraverserFactory.Implementation excludeTypes(
			final Iterable<Class<?>> excludedTypes
		)
		{
			ObjectGraphTraverserFactory.super.excludeTypes(excludedTypes);
			return this;
		}



		@Override
		public synchronized ObjectGraphTraverser buildObjectGraphTraverser()
		{
			return ObjectGraphTraverser.New(
				this.provideTraversalHandlerProvider(),
				this.provideSkipped()                 ,
				this.provideAlreadyHandledProvider()
			);
		}
	}

}
