package one.microstream.persistence.types;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.exceptions.PersistenceException;

public interface PersistenceTypeMismatchValidator<M>
{
	public void validateTypeMismatches(
		PersistenceTypeDictionary                  typeDictionary         ,
		XGettingEnum<PersistenceTypeHandler<M, ?>> unmatchableTypeHandlers
	);
	
	
	
	public static <M> PersistenceTypeMismatchValidator.Failing<M> Failing()
	{
		return new PersistenceTypeMismatchValidator.Failing<>();
	}
	
	public static <M> PersistenceTypeMismatchValidator.NoOp<M> NoOp()
	{
		return new PersistenceTypeMismatchValidator.NoOp<>();
	}
	
	public final class Failing<M> implements PersistenceTypeMismatchValidator<M>
	{

		@Override
		public void validateTypeMismatches(
			final PersistenceTypeDictionary                  typeDictionary         ,
			final XGettingEnum<PersistenceTypeHandler<M, ?>> unmatchableTypeHandlers
		)
		{
			if(unmatchableTypeHandlers.isEmpty())
			{
				return;
			}
			
			final VarString vs = VarString.New("[");
			unmatchableTypeHandlers.iterate(th -> vs.add(',').add(th.type().getName()));
			vs.deleteLast().setLast(']');
			
			// (21.05.2018 TM)EXCP: proper exception
			throw new PersistenceException("Persistence type definition mismatch for the following types: " + vs);
			
		}
	}
	
	public final class NoOp<M> implements PersistenceTypeMismatchValidator<M>
	{

		@Override
		public void validateTypeMismatches(
			final PersistenceTypeDictionary                  typeDictionary         ,
			final XGettingEnum<PersistenceTypeHandler<M, ?>> unmatchableTypeHandlers
		)
		{
			// no-op
		}
	}
	
}
