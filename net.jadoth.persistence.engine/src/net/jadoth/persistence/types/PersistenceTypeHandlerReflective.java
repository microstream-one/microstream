package net.jadoth.persistence.types;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;

public interface PersistenceTypeHandlerReflective<M, T> extends PersistenceTypeHandlerGeneric<M, T>
{
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberField<T>> members();
	
	public XGettingEnum<Field> instanceFields();

	public XGettingEnum<Field> instancePrimitiveFields();

	public XGettingEnum<Field> instanceReferenceFields();
}
