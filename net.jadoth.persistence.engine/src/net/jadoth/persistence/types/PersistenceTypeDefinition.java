package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.swizzling.types.SwizzleTypeIdentity;
import net.jadoth.swizzling.types.SwizzleTypeLink;

public interface PersistenceTypeDefinition<T> extends SwizzleTypeIdentity, SwizzleTypeLink<T>
{
	@Override
	public long typeId();

	@Override
	public String typeName();

	@Override
	public Class<T> type();

	public PersistenceTypeDescription typeDescription();



	public static PersistenceTypeDefinition<?> resolveType(final PersistenceTypeDescription typeDescription)
	{
		try
		{
			final Class<?> type = JadothReflect.classForName(typeDescription.typeName());
			return new PersistenceTypeDefinition.Implementation<>(type, typeDescription);
		}
		catch(final ReflectiveOperationException e)
		{
			throw new RuntimeException(e); // (05.04.2013)EXCP: proper exception
		}
	}



	public final class Implementation<T> implements PersistenceTypeDefinition<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final Class<T>                   type           ;
		private final PersistenceTypeDescription typeDescription;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final Class<T> type, final PersistenceTypeDescription typeDescription)
		{
			super();
			this.type            = notNull(type)           ;
			this.typeDescription = notNull(typeDescription);
		}

		@Override
		public long typeId()
		{
			return this.typeDescription.typeId();
		}

		@Override
		public String typeName()
		{
			return this.typeDescription.typeName();
		}

		@Override
		public Class<T> type()
		{
			return this.type;
		}

		@Override
		public PersistenceTypeDescription typeDescription()
		{
			return this.typeDescription;
		}

	}

}
