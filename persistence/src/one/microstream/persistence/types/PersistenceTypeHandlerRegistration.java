package one.microstream.persistence.types;

@FunctionalInterface
public interface PersistenceTypeHandlerRegistration<M>
{
	public void registerTypeHandlers(
		PersistenceCustomTypeHandlerRegistry<M> customTypeHandlerRegistry ,
		PersistenceSizedArrayLengthController   sizedArrayLengthController
	);
}
