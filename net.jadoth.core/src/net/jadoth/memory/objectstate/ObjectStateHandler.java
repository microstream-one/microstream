package net.jadoth.memory.objectstate;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;

public interface ObjectStateHandler<T> extends ObjectStateDescriptor<T>, ObjectStateComparer<T>
{
	public ObjectStateDescriptor<T> getStateDescriptor();



	@Override
	public Class<T> type();

	@Override
	public XGettingEnum<Field> getInstanceFields();

	@Override
	public XGettingEnum<Field> getInstancePrimitiveFields();

	@Override
	public XGettingEnum<Field> getInstanceReferenceFields();

//	@Override
//	public void copy(T source, T target);

	/* (28.05.2013)XXX: generic "deep equal" really reasonable?
	 * = compare whole object graph
	 * even with potential indefnite loop as there's no already handled set
	 * make only shallow at best or remove completely
	 */
	@Override
	public boolean isEqual(T source, T target, ObjectStateHandlerLookup instanceStateHandlerLookup);

}
