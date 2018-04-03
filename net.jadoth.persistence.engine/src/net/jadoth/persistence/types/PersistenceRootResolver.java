package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.keyValue;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XImmutableTable;
import net.jadoth.memory.Memory;
import net.jadoth.util.KeyValue;

public interface PersistenceRootResolver
{
	public default Object resolveRootInstance(final String identifier)
	{
		return Memory.getStaticReference(resolveField(identifier));
	}

	public default String deriveIdentifier(final Field field)
	{
		return field.getDeclaringClass().getName() + fieldIdentifierDelimiter() + field.getName();
	}

	/**
	 * Iterates all entries that are explicitely known to this instance (e.g. custom mapped override entries).
	 *
	 * @param procedure
	 */
	public default void iterateEntries(final Consumer<? super KeyValue<String, ?>> procedure)
	{
		// no entries in stateless default implementation
	}

	
	
	public static char fieldIdentifierDelimiter()
	{
		return '#';
	}

	public static Field resolveField(final String identifier)
	{
		final int index = identifier.lastIndexOf(fieldIdentifierDelimiter());
		if(index < 0)
		{
			throw new IllegalArgumentException(); // (16.10.2013 TM)TODO: proper Exception
		}
		
		final String classString = identifier.substring(0, index);
		final String fieldName   = identifier.substring(index + 1);

		try
		{
			final Class<?> declaringClass = Class.forName(classString);
			return declaringClass.getDeclaredField(fieldName);
		}
		catch(final Exception e)
		{
			throw new IllegalArgumentException(e); // (16.10.2013 TM)TODO: proper Exception
		}
	}
	
	
	public static PersistenceRootResolver New()
	{
		return new Stateless();
	}
	
	public static PersistenceRootResolver New(final String identifier, final Object instance)
	{
		// hardcoded table implementation to ensure value-equality.
		return new Overriding(
			EqConstHashTable.New(
				keyValue(identifier, instance)
			)
		);
	}
	
	public static PersistenceRootResolver New(final XGettingMap<String, ?> identifierOverrides)
	{
		// hardcoded table implementation to ensure value-equality.
		return new Overriding(
			EqConstHashTable.New(identifierOverrides)
		);
	}
	
	
	public final class Stateless implements PersistenceRootResolver
	{
		/*
		 * A stateless class with all-default-methods interface(s) contains no source code.
		 * In other words: since default methods, java is missing a mechanism 
		 * to create (stateless) instances of interfaces.
		 */
	}
	
	public final class Overriding implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XImmutableTable<String, ?> identifierOverrides;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Overriding(final XImmutableTable<String, ?> identifierOverrides)
		{
			super();
			this.identifierOverrides = identifierOverrides;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final Object resolveRootInstance(final String identifier)
		{
			final Object overrideInstance = this.identifierOverrides.get(identifier);
			return overrideInstance != null
				? overrideInstance
				: PersistenceRootResolver.super.resolveRootInstance(identifier)
			;
		}

		@Override
		public final void iterateEntries(final Consumer<? super KeyValue<String, ?>> procedure)
		{
			this.identifierOverrides.iterate(procedure);
		}

	}

}
