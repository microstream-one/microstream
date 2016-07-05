package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.keyValue;
import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Function;

import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XImmutableTable;
import net.jadoth.memory.Memory;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.util.KeyValue;

public interface PersistenceRootResolver extends Function<String, Object>
{
	@Override
	public Object apply(String input);

	public String toIdentifier(Field field);

	/**
	 * Iterates all entries that are explicitely known to this instance (e.g. custom mapped override entries).
	 *
	 * @param procedure
	 */
	public void iterateKnownEntries(Consumer<? super KeyValue<String, ?>> procedure);


	static final PersistenceRootResolver DEFAULT_RESOLVER =
		new PersistenceRootResolver()
		{
			@Override
			public Object apply(final String input)
			{
				return Static.resolveIdentifier(input);
			}

			@Override
			public String toIdentifier(final Field field)
			{
				return Static.toIdentifier(field);
			}

			@Override
			public void iterateKnownEntries(final Consumer<? super KeyValue<String, ?>> procedure)
			{
				// no-op, no explicit entries
			}

		}
	;


	/**
	 * A simple implementation that allows one override mapping (e.g. application entity graph root)
	 * and otherwise relays to the default. Anything more complex is best implemented in an
	 * application-specific tailored way.
	 *
	 * @author Thomas Muenz
	 *
	 */
	public final class SingleOverride implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String identifier;
		final Object instance  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public SingleOverride(final String identifier, final Object instance)
		{
			super();
			this.identifier = notNull(identifier);
			this.instance   = notNull(instance)  ;
		}


		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final Object apply(final String input)
		{
			return this.identifier.equals(input)  ? this.instance : DEFAULT_RESOLVER.apply(input);
		}

		@Override
		public final String toIdentifier(final Field field)
		{
			return DEFAULT_RESOLVER.toIdentifier(field);
		}

		@Override
		public final void iterateKnownEntries(final Consumer<? super KeyValue<String, ?>> procedure)
		{
			procedure.accept(
				keyValue(this.identifier, this.instance)
			);
		}

	}

	public final class MappedOverride implements PersistenceRootResolver
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XImmutableTable<String, ?> identifierOverrides;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public MappedOverride(final XGettingTable<String, ?> identifierOverrides)
		{
			super();
			// make sure to use value-equality implementation internally
			this.identifierOverrides = EqConstHashTable.New(identifierOverrides);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final Object apply(final String input)
		{
			final Object overrideInstance = this.identifierOverrides.get(input);
			return overrideInstance != null ? overrideInstance : DEFAULT_RESOLVER.apply(input);
		}

		@Override
		public final String toIdentifier(final Field field)
		{
			return DEFAULT_RESOLVER.toIdentifier(field);
		}

		@Override
		public final void iterateKnownEntries(final Consumer<? super KeyValue<String, ?>> procedure)
		{
			this.identifierOverrides.iterate(procedure);
		}

	}


	public final class Static
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		static final char FIELD_IDENTIFIER_DELIMITER = '#';



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static String toIdentifier(final Field field)
		{
			return field.getDeclaringClass().getName() + FIELD_IDENTIFIER_DELIMITER + field.getName();
		}

		public static Field resolveField(final String identifier)
		{
			final int index = identifier.lastIndexOf(FIELD_IDENTIFIER_DELIMITER);
			if(index < 0)
			{
				throw new IllegalArgumentException(); // (16.10.2013 TM)TODO: proper Exception
			}
			final String classString = identifier.substring(0, index);
			final String fieldName   = identifier.substring(index + 1);

			final Class<?> c;
			try
			{
				c = Class.forName(classString);
				return JadothReflect.getAnyField(c, fieldName);
			}
			catch(final Exception e)
			{
				throw new IllegalArgumentException(e); // (16.10.2013 TM)TODO: proper Exception
			}
		}

		public static final Object resolveIdentifier(final String identifier)
		{
			return Memory.getStaticReference(resolveField(identifier));
		}



		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}

	}

}
