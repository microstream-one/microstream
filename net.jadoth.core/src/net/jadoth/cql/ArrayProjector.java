package net.jadoth.cql;

import java.util.function.Function;

import net.jadoth.collections.JadothArrays;
import net.jadoth.functional.JadothFunctions;

public interface ArrayProjector<T> extends Function<T, Object[]>
{
	@Override
	public Object[] apply(T t);
	
	
	
	@SafeVarargs
	public static <T> ArrayProjector<T> New(final Function<? super T, Object>... fieldProjectors)
	{
		final Function<? super T, Object>[] nonNulls = JadothArrays.newArrayBySample(fieldProjectors);
		
		for(int i = 0; i < fieldProjectors.length; i++)
		{
			nonNulls[i] = fieldProjectors[i] != null ? fieldProjectors[i] : JadothFunctions.toNull();
		}
		
		return new Implementation<>(fieldProjectors);
	}
	
	public final class Implementation<T> implements ArrayProjector<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Function<? super T, Object>[] fieldProjectors;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final Function<? super T, Object>[] fieldProjectors)
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
