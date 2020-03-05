package one.microstream.persistence.types;

public interface PersistenceLegacyTypeHandlingListener<D>
{
	public <T> void registerLegacyTypeHandlingCreation(
		long                         objectId             ,
		T                            instance             ,
		PersistenceTypeDefinition    legacyTypeDescription,
		PersistenceTypeHandler<D, T> currentTypeHandler
	);
	
	/* note:
	 * further listening wishes (before creation, before/after update or completion, or whatever)
	 * can be implemented by using a LegacyTypeHandler wrapper implementing inserted by a
	 * wrapping LegacyTypeHandlerCreator. That approach makes this explicit listener almost redundant,
	 * but it is a little easier and nicer to have the basic creation listening in this concrete way.
	 * All beyond-basic needs are better implemented with the wrapping approach.
	 */
}
