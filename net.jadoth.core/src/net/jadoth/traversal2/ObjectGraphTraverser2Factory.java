package net.jadoth.traversal2;

import java.util.function.Function;

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
		protected synchronized TraversalHandlerProvider provideTraversalHandlerProvider()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectTraverserFactory.Implementation#provideTraversalHandlerProvider()
		}
		
		protected synchronized XGettingCollection<Object> provideSkipped()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectTraverserFactory.Implementation#provideSkipped()
//			return this.skipped;
		}
		
		protected synchronized Function<XGettingCollection<Object>, XSet<Object>>  provideAlreadyHandledProvider()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectTraverserFactory.Implementation#provideAlreadyHandledProvider()
//			if(this.alreadyHandledProvider != null)
//			{
//				return this.alreadyHandledProvider;
//			}
//
//			return OpenAdressingMiniSet::New;
		}
		
		protected synchronized TraversalAcceptor provideAcceptor()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ObjectGraphTraverser2Factory.Implementation#provideAcceptor()
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
