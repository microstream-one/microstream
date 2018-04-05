package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.keyValue;
import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XImmutableTable;
import net.jadoth.memory.Memory;
import net.jadoth.util.KeyValue;

public interface PersistenceRootResolver
{
	public default Result resolveRootInstance(final String identifier)
	{
		final Field field;
		try
		{
			field = resolveField(identifier);
		}
		catch(final ReflectiveOperationException e)
		{
			throw new IllegalArgumentException(e);
		}
		
		return PersistenceRootResolver.createResult(getStaticReference(field), identifier);
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
	
	
	public static Object getStaticReference(final Field field)
	{
		return Memory.getStaticReference(field);
	}

	public static Result createResult(
		final Object resolvedRootInstance,
		final String identifier
	)
	{
		return PersistenceRootResolver.createResult(resolvedRootInstance, identifier, identifier);
	}
	
	public static Result createResult(
		final Object resolvedRootInstance,
		final String providedIdentifier  ,
		final String resolvedIdentifier
	)
	{
		return new Result.Implementation(
			notNull(resolvedRootInstance),
			notNull(providedIdentifier)  ,
			notNull(resolvedIdentifier)
		);
	}

	public interface Result
	{
		public Object resolvedRootInstance();
		
		public String providedIdentifier();
		
		public String resolvedIdentifier();
		
		public default boolean hasChanged()
		{
			// it is assumed that for the unchanged case, the same identifier String is passed twice.
			return this.providedIdentifier() == this.resolvedIdentifier();
		}
		
		public final class Implementation implements PersistenceRootResolver.Result
		{
			private final Object resolvedRootInstance;
			private final String providedIdentifier  ;
			private final String resolvedIdentifier  ;
			
			Implementation(
				final Object resolvedRootInstance,
				final String providedIdentifier  ,
				final String resolvedIdentifier
			)
			{
				super();
				this.resolvedRootInstance = resolvedRootInstance;
				this.providedIdentifier   = providedIdentifier  ;
				this.resolvedIdentifier   = resolvedIdentifier  ;
			}

			@Override
			public final Object resolvedRootInstance()
			{
				return this.resolvedRootInstance;
			}

			@Override
			public String providedIdentifier()
			{
				return this.providedIdentifier;
			}

			@Override
			public String resolvedIdentifier()
			{
				return this.resolvedIdentifier;
			}
			
		}
		
	}

	
	
	public static char fieldIdentifierDelimiter()
	{
		return '#';
	}
	
	public static int getFieldIdentifierDelimiterIndex(final String identifier)
	{
		final int index = identifier.lastIndexOf(fieldIdentifierDelimiter());
		if(index < 0)
		{
			throw new IllegalArgumentException(); // (16.10.2013 TM)TODO: proper Exception
		}
		
		return index;
	}
	
	public static String getClassName(final String identifier)
	{
		return identifier.substring(0, PersistenceRootResolver.getFieldIdentifierDelimiterIndex(identifier));
	}
	
	public static String getFieldName(final String identifier)
	{
		return identifier.substring(PersistenceRootResolver.getFieldIdentifierDelimiterIndex(identifier) + 1);
	}

	public static Field resolveField(final String identifier)
		throws ClassNotFoundException, NoSuchFieldException
	{
		return resolveField(
			PersistenceRootResolver.getClassName(identifier),
			PersistenceRootResolver.getFieldName(identifier)
		);
	}
	
	public static Field resolveField(final String className, final String fieldName)
		throws ClassNotFoundException, NoSuchFieldException
	{
		// ReflectiveOperationExceptions have to be passed to the calling context for retryin
		final Class<?> declaringClass = Class.forName(className);
		return declaringClass.getDeclaredField(fieldName);
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
			),
			X.emptyTable()
		);
	}
	
	public static PersistenceRootResolver New(final XGettingMap<String, ?> identifierOverrides)
	{
		// hardcoded table implementation to ensure value-equality.
		return new Overriding(
			EqConstHashTable.New(identifierOverrides),
			X.emptyTable()
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

		final XImmutableTable<String, ?>      identifierOverrides;
		final XImmutableTable<String, String> identifierMappings ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Overriding(
			final XImmutableTable<String, ?>      identifierOverrides,
			final XImmutableTable<String, String> identifierMappings
		)
		{
			super();
			this.identifierOverrides = identifierOverrides;
			this.identifierMappings  = identifierMappings ;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final Result resolveRootInstance(final String identifier)
		{
			final Object overrideInstance = this.identifierOverrides.get(identifier);
			if(overrideInstance != null)
			{
				return PersistenceRootResolver.createResult(overrideInstance, identifier);
			}
			
			final String className = PersistenceRootResolver.getClassName(identifier);
			final String fieldName = PersistenceRootResolver.getFieldName(identifier);
			
			try
			{
				return PersistenceRootResolver.createResult(
					PersistenceRootResolver.getStaticReference(
						PersistenceRootResolver.resolveField(className, fieldName)
					),
					identifier
				);
			}
			catch(final ReflectiveOperationException e)
			{
				// (05.04.2018 TM)FIXME: check map
				try
				{
					return PersistenceRootResolver.createResult(
						PersistenceRootResolver.getStaticReference(
							PersistenceRootResolver.resolveField(className, fieldName)
						),
						identifier
					);
				}
				catch(final ReflectiveOperationException e1)
				{
					// the mapping entry is invalid (maybe outdated), too.
					throw new IllegalArgumentException(e1);
				}
			}
		}

		@Override
		public final void iterateEntries(final Consumer<? super KeyValue<String, ?>> procedure)
		{
			this.identifierOverrides.iterate(procedure);
		}

	}

}
