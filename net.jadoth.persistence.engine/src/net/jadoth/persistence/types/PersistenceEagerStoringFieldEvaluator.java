package net.jadoth.persistence.types;

import java.lang.reflect.Field;

@FunctionalInterface
public interface PersistenceEagerStoringFieldEvaluator
{
	public boolean isEagerStoring(Class<?> t, Field u);

}