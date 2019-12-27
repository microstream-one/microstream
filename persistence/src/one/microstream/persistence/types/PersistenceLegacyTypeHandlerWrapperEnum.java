package one.microstream.persistence.types;

import static one.microstream.X.notNull;

public class PersistenceLegacyTypeHandlerWrapperEnum<M, T>
extends PersistenceLegacyTypeHandlerWrapper<M, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <M, T> PersistenceLegacyTypeHandlerWrapperEnum<M, T> New(
		final PersistenceTypeDefinition    legacyTypeDefinition,
		final PersistenceTypeHandler<M, T> currentTypeHandler  ,
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
		final PersistenceTypeHandler<M, T> currentTypeHandler  ,
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
	public T create(final M medium, final PersistenceLoadHandler handler)
	{
		// this is all there is on this level for this implementation / case.
		return PersistenceLegacyTypeHandler.resolveEnumConstant(this, medium, this.ordinalMapping);
	}
	
}
