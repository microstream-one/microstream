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
import java.util.Date;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.collections.ConstHashEnum;
import net.jadoth.collections.ConstHashTable;
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingSet;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XReplacingBag;
import net.jadoth.collections.types.XSet;

public interface ObjectGraphTraverser2Factory
{
	/* (29.06.2017 TM)TODO: ObjectTraverserFactory
	 * - see old implementation (leaf types, util methods, etc.)
	 * -
	 */

	public ObjectGraphTraverser2 buildObjectGraphTraverser();
	
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
	
	
	
	public static XGettingTable<Class<?>, TraversalHandler> defaultPolymorphTypeTraversalHandlers()
	{
		return ConstHashTable.<Class<?>, TraversalHandler>New(
			keyValue(XReplacingBag.class, new TraverserXCollection()),
			keyValue(Object[].class     , new TraverserArray()      )
		);
	}
	
	public static XGettingTable<Class<?>, TraversalHandler> defaultConcreteTypeTraversalHandlers()
	{
		return ConstHashTable.<Class<?>, TraversalHandler>New(
			/* empty so far */
		);
	}
	
	public final class Implementation implements ObjectGraphTraverser2Factory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final HashEnum<Object>                      skipped                  = HashEnum.New() ;
		private final HashEnum<Class<?>>                    leafTypes                = HashEnum.New() ;
		private final HashTable<Object, TraversalHandler>   handlersPerInstance      = HashTable.New();
		private final HashTable<Class<?>, TraversalHandler> handlersPerConcreteType  = HashTable.New();
		private final HashTable<Class<?>, TraversalHandler> handlersPerPolymorphType = HashTable.New();
		
		private Predicate<? super Field>                           traversableFieldSelector;
		private Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ;
		private TraversalAcceptor                                  acceptor                ;
		private TraversalHandlerCreator                            traversalHandlerCreator ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected synchronized TraversalHandlerProvider provideTraversalHandlerProvider()
		{
			return TraversalHandlerProvider.New(
				handlersPerInstance,
				null,
				null,
				leafTypes,
				traversalHandlerCreator
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
		
		protected synchronized TraversalAcceptor provideAcceptor()
		{
			return this.acceptor;
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
	}
	
}
