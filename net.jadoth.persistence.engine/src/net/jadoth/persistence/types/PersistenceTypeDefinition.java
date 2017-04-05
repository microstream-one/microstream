package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

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

	public PersistenceTypeDescription<T> typeDescription();


	
	public static <T> PersistenceTypeDefinition<T> New(
		final Class<T>                      type           ,
		final PersistenceTypeDescription<T> typeDescription
	)
	{
		return new PersistenceTypeDefinition.Implementation<>(
			notNull(type)           ,
			notNull(typeDescription)
		);
	}
	
	public final class Implementation<T> implements PersistenceTypeDefinition<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final Class<T>                      type           ;
		private final PersistenceTypeDescription<T> typeDescription;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final Class<T> type, final PersistenceTypeDescription<T> typeDescription)
		{
			super();
			this.type            = type           ;
			this.typeDescription = typeDescription;
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
		public PersistenceTypeDescription<T> typeDescription()
		{
			return this.typeDescription;
		}

	}

}
