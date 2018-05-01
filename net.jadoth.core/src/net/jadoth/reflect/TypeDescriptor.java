package net.jadoth.reflect;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;

public interface TypeDescriptor<T>
{
	public Class<T> type();

	public XGettingEnum<Field> getInstanceFields();

	public XGettingEnum<Field> getInstancePrimitiveFields();

	public XGettingEnum<Field> getInstanceReferenceFields();

	public boolean hasInstanceReferences();
}
