package one.microstream.persistence.types;

import java.lang.reflect.Field;

import one.microstream.collections.types.XGettingEnum;

public interface PersistenceTypeHandlerReflective<M, T> extends PersistenceTypeHandlerGeneric<M, T>
{
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> instanceMembers();
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> storingMembers();
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> settingMembers();
	
	public XGettingEnum<Field> instanceFields();

	public XGettingEnum<Field> instancePrimitiveFields();

	public XGettingEnum<Field> instanceReferenceFields();
}
