package one.microstream.util.cql;

import static one.microstream.X.notNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.types.XIterable;

public interface CqlResulting<O, R extends Consumer<O> & XIterable<O>>
{
	public R target();
	
	public default <I> CqlQuery<I, O, R> from(final XIterable<? extends I> source)
	{
		return CqlQuery.New(source, null, null, null, null, null, CqlResultor.New(this.target()));
	}
	
	public default <I> CqlQuery<I, O, R> select(final Predicate<? super I> selector)
	{
		return CqlQuery.New(null, null, null, selector, null, null, CqlResultor.New(this.target()));
	}
	
	public default <I> CqlQuery<I, O, R> project(final Function<? super I, O> projector)
	{
		return CqlQuery.New(null, null, null, null, projector, null, CqlResultor.New(this.target()));
	}
	
	
	
	public static <O, R extends Consumer<O> & XIterable<O>> CqlResulting<O, R> New(final R target)
	{
		return new CqlResulting.Default<>(
			notNull(target)
		);
	}
	
	public final class Default<O, R extends Consumer<O> & XIterable<O>> implements CqlResulting<O, R>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final R target;

		public Default(final R target)
		{
			super();
			this.target = target;
		}
		
		@Override
		public R target()
		{
			return this.target;
		}
		
	}
	
}
