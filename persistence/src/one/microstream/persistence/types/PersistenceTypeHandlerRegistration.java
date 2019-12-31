package one.microstream.persistence.types;

@FunctionalInterface
public interface PersistenceTypeHandlerRegistration<D>
{
	public void registerTypeHandlers(
		PersistenceCustomTypeHandlerRegistry<D> customTypeHandlerRegistry ,
		PersistenceSizedArrayLengthController   sizedArrayLengthController
	);
}
