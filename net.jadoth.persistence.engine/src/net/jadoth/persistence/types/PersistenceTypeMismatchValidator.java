package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingEnum;

public interface PersistenceTypeMismatchValidator<M>
{
	public void validateTypeMismatches(
		PersistenceTypeDictionary                  typeDictionary         ,
		XGettingEnum<PersistenceTypeHandler<M, ?>> unmatchableTypeHandlers
	);
}
