package one.microstream.persistence.types;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.exceptions.PersistenceException;

public interface PersistenceTypeMismatchValidator<D>
{
	public void validateTypeMismatches(
		PersistenceTypeDictionary                  typeDictionary         ,
		XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
	);
	
	
	
	public static <D> PersistenceTypeMismatchValidator.Failing<D> Failing()
	{
		return new PersistenceTypeMismatchValidator.Failing<>();
	}
	
	public static <D> PersistenceTypeMismatchValidator.NoOp<D> NoOp()
	{
		return new PersistenceTypeMismatchValidator.NoOp<>();
	}
	
	public final class Failing<D> implements PersistenceTypeMismatchValidator<D>
	{

		@Override
		public void validateTypeMismatches(
			final PersistenceTypeDictionary                  typeDictionary         ,
			final XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
		)
		{
			if(unmatchableTypeHandlers.isEmpty())
			{
				return;
			}
			
			final VarString vs = VarString.New("[");
			unmatchableTypeHandlers.iterate(th -> vs.add(',').add(th.type().getName()));
			vs.deleteLast().setLast(']');
			
			throw new PersistenceException("Persistence type definition mismatch for the following types: " + vs);
			
		}
	}
	
	public final class NoOp<D> implements PersistenceTypeMismatchValidator<D>
	{

		@Override
		public void validateTypeMismatches(
			final PersistenceTypeDictionary                  typeDictionary         ,
			final XGettingEnum<PersistenceTypeHandler<D, ?>> unmatchableTypeHandlers
		)
		{
			// no-op
		}
	}
	
}
