package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.keyValue;
import static net.jadoth.Jadoth.mayNull;
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
		return buildFieldIdentifier(field.getDeclaringClass().getName(), field.getName());
	}
	
	public static String buildFieldIdentifier(final String className, final String fieldName)
	{
		return className + fieldIdentifierDelimiter() + fieldName;
	}

	/**
	 * Iterates all entries that are explicitely known to this instance (e.g. custom mapped override entries).
	 *
	 * @param procedure
	 */
	public default void iterateIdentifierMappings(final Consumer<? super KeyValue<String, ?>> procedure)
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
		// if an explicit delete entry was found, the instance and the resolved identifier are null.
		return new Result.Implementation(
			mayNull(resolvedRootInstance),
			notNull(providedIdentifier)  ,
			mayNull(resolvedIdentifier)
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
		return new Implementation(
			EqConstHashTable.New(
				keyValue(identifier, instance)
			),
			X.emptyTable()
		);
	}
	
	public static PersistenceRootResolver New(final XGettingMap<String, ?> identifierMapping)
	{
		// hardcoded table implementation to ensure value-equality.
		return new Implementation(
			EqConstHashTable.New(identifierMapping),
			X.emptyTable()
		);
	}
	
	public static PersistenceRootResolver New(
		final String                      identifier         ,
		final Object                      instance           ,
		final XGettingMap<String, String> refactoringMappings
	)
	{
		// hardcoded table implementation to ensure value-equality.
		return new Implementation(
			EqConstHashTable.New(
				keyValue(identifier, instance)
			),
			X.emptyTable()
		);
	}
	
	public static PersistenceRootResolver New(
		final XGettingMap<String, ?>      identifierMappings ,
		final XGettingMap<String, String> refactoringMappings
	)
	{
		// hardcoded table implementation to ensure value-equality.
		return new Implementation(
			EqConstHashTable.New(identifierMappings) ,
			EqConstHashTable.New(refactoringMappings)
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
	
	public final class Implementation implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XImmutableTable<String, ?>      identifierMappings ;
		final XImmutableTable<String, String> refactoringMappings;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final XImmutableTable<String, ?>      identifierMappings ,
			final XImmutableTable<String, String> refactoringMappings
		)
		{
			super();
			this.identifierMappings  = identifierMappings ;
			this.refactoringMappings = refactoringMappings;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void iterateIdentifierMappings(final Consumer<? super KeyValue<String, ?>> procedure)
		{
			this.identifierMappings.iterate(procedure);
		}
		
		final Result tryResolveRootInstance(final String originalIdentifier, final String effectiveIdentifier)
			throws ClassNotFoundException, NoSuchFieldException
		{
			final Object overrideInstance = this.identifierMappings.get(effectiveIdentifier);
			if(overrideInstance != null)
			{
				return PersistenceRootResolver.createResult(overrideInstance, effectiveIdentifier);
			}
			
			final String className = PersistenceRootResolver.getClassName(effectiveIdentifier);
			final String fieldName = PersistenceRootResolver.getFieldName(effectiveIdentifier);
			
			return PersistenceRootResolver.createResult(
				PersistenceRootResolver.getStaticReference(
					PersistenceRootResolver.resolveField(className, fieldName)
				),
				originalIdentifier,
				effectiveIdentifier
			);
		}
		
		@Override
		public final Result resolveRootInstance(final String identifier)
		{
			/*
			 * Mapping lookups take precedence over the direct resolving attempt.
			 * This is important to enable refactorings that switch names.
			 * E.g.:
			 * A -> B
			 * C -> A
			 * However, this also increases the responsibility of the developer who defines the mapping:
			 * The mapping has to be removed after the first usage, otherwise the new instance under the old name
			 * is mapped to the old name's new name, as well. (In the example: after two executions, both instances
			 * would be mapped to B, which is an error. However, the source of the error is not a bug,
			 * but an outdated mapping rule defined by the using developer).
			 */
			
			// mapping variant #1: completely mapped identifier (className#fieldName or arbitrary name, e.g. "root")
			if(this.refactoringMappings.keys().contains(identifier))
			{
				final String mappedIdentifier = this.refactoringMappings.get(identifier);
				if(mappedIdentifier == null)
				{
					// an identifier explicitely mapped to null means the element has been deleted.
					return PersistenceRootResolver.createResult(null, identifier, null);
				}
				
				try
				{
					return tryResolveRootInstance(identifier, mappedIdentifier);
				}
				catch(final ReflectiveOperationException e)
				{
					// check next case.
				}
			}
			
			/*
			 * identifier mappings must be checked before trying to extract a reflection class name in case
			 * they are arbitrary strings (e.g. "root") instead of reflective identifiers.
			 */
			final Object overrideInstance = this.identifierMappings.get(identifier);
			if(overrideInstance != null)
			{
				return PersistenceRootResolver.createResult(overrideInstance, identifier);
			}

			// mapping variant #2: only mapped className (fieldName remains the same)
			final String className = PersistenceRootResolver.getClassName(identifier);
			if(this.refactoringMappings.keys().contains(className))
			{
				final String mappedClassName = this.refactoringMappings.get(className);
				if(mappedClassName == null)
				{
					// a className explicitely mapped to null means it has been deleted.
					return PersistenceRootResolver.createResult(null, identifier, null);
				}
				
				final String fieldName        = PersistenceRootResolver.getFieldName(identifier);
				final String mappedIdentifier = PersistenceRootResolver.buildFieldIdentifier(mappedClassName, fieldName);
				try
				{
					return tryResolveRootInstance(identifier, mappedIdentifier);
				}
				catch(final ReflectiveOperationException e)
				{
					// check next case
				}
			}
			
			// no mapping could be found, so the only remaining option is to resolve the identifier in a general way.
			return PersistenceRootResolver.super.resolveRootInstance(identifier);
		}

	}

}
