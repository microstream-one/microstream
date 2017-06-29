package net.jadoth.traversal2;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XSet;

public interface ObjectGraphTraverser2Factory
{
	/* (29.06.2017 TM)TODO: ObjectTraverserFactory
	 * - see old implementation (leaf types, util methods, etc.)
	 * -
	 */

	public ObjectGraphTraverser2 buildObjectGraphTraverser();
	
	
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
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectTraverserFactory.Implementation#provideTraversalHandlerProvider()
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
