package one.microstream.persistence.types;

import static one.microstream.X.notNull;

public class PersistenceLegacyTypeHandlerWrapperEnum<D, T>
extends PersistenceLegacyTypeHandlerWrapper<D, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <D, T> PersistenceLegacyTypeHandlerWrapperEnum<D, T> New(
		final PersistenceTypeDefinition    legacyTypeDefinition,
		final PersistenceTypeHandler<D, T> currentTypeHandler  ,
		final Integer[]                    ordinalMapping
	)
	{
		return new PersistenceLegacyTypeHandlerWrapperEnum<>(
			notNull(legacyTypeDefinition),
			notNull(currentTypeHandler),
			notNull(ordinalMapping)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Integer[] ordinalMapping;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	PersistenceLegacyTypeHandlerWrapperEnum(
		final PersistenceTypeDefinition    legacyTypeDefinition,
		final PersistenceTypeHandler<D, T> currentTypeHandler  ,
		final Integer[]                    ordinalMapping
	)
	{
		super(legacyTypeDefinition, currentTypeHandler);
		this.ordinalMapping = ordinalMapping;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public T create(final D data, final PersistenceLoadHandler handler)
	{
		// this is all there is on this level for this implementation / case.
		return PersistenceLegacyTypeHandler.resolveEnumConstant(this, data, this.ordinalMapping);
	}
	
}
