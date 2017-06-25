package net.jadoth.swizzling.types;

public final class TraversalHandlerLazy
{}
//extends TraversalHandler.AbstractImplementation<Lazy<?>>
//{
//	protected TraversalHandlerLazy(final Predicate<? super Lazy<?>> logic)
//	{
//		super(logic);
//	}
//
//	@Override
//	public void traverseReferences(final Lazy<?> instance, final Consumer<Object> referenceHandler)
//	{
//		// the loader reference is a meta helper that is no actual entity, so it is ignored.
//		referenceHandler.accept(instance.get());
//	}
//
//
//	public static final class Provider implements TraversalHandlerCustomProvider<Lazy<?>>
//	{
//		@Override
//		public TraversalHandler<Lazy<?>> provideTraversalHandler(
//			final Class<? extends Lazy<?>>       type         ,
//			final TraversalHandlingLogicProvider logicProvider
//		)
//		{
//			/*
//			 * this is guaranteed by the using logic, but just in case.
//			 * Performance doesn't matter in one-time analyzing logic.
//			 */
//			if(type != Lazy.class)
//			{
//				throw new IllegalArgumentException();
//			}
//
//			return new TraversalHandlerLazy(logicProvider.provideHandlingLogic(type));
//		}
//
//	}
//
//}
