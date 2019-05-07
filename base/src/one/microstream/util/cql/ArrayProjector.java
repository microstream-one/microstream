package one.microstream.util.cql;

import java.util.function.Function;

import one.microstream.X;
import one.microstream.functional.XFunc;

public interface ArrayProjector<T> extends Function<T, Object[]>
{
	@Override
	public Object[] apply(T t);
	
	
	
	@SafeVarargs
	public static <T> ArrayProjector<T> New(final Function<? super T, Object>... fieldProjectors)
	{
		final Function<? super T, Object>[] nonNulls = X.ArrayOfSameType(fieldProjectors);
		
		for(int i = 0; i < fieldProjectors.length; i++)
		{
			nonNulls[i] = fieldProjectors[i] != null
				? fieldProjectors[i]
				: XFunc.toNull()
			;
		}
		
		return new Default<>(fieldProjectors);
	}
	
	public final class Default<T> implements ArrayProjector<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Function<? super T, Object>[] fieldProjectors;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final Function<? super T, Object>[] fieldProjectors)
		{
			super();
			this.fieldProjectors = fieldProjectors;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final Object[] apply(final T t)
		{
			final Function<? super T, Object>[] fieldProjectors = this.fieldProjectors              ;
			final Object[]                      result          = new Object[fieldProjectors.length];
			
			for(int i = 0; i < result.length; i++)
			{
				result[i] = fieldProjectors[i].apply(t);
			}
			
			return result;
		}
		
	}
	
}
