package one.microstream.persistence.types;

import static one.microstream.X.notNull;

public interface PersistenceLegacyTypeMapperLogging<D>
	extends PersistenceLegacyTypeMapper<D>, PersistenceLoggingWrapper<PersistenceLegacyTypeMapper<D>>
{

	static PersistenceLegacyTypeMapperLogging<?> New(final PersistenceLegacyTypeMapper<?> wrapped)
	{
		return new Default<>(notNull(wrapped));
	}

	public class Default<D>
		extends PersistenceLoggingWrapper.Abstract<PersistenceLegacyTypeMapper<D>>
		implements PersistenceLegacyTypeMapperLogging<D>
	{

		protected Default(final PersistenceLegacyTypeMapper<D> wrapped)
		{
			super(wrapped);
		}
	
		@Override
		public <T> PersistenceLegacyTypeHandler<D, T> ensureLegacyTypeHandler(
			final PersistenceTypeDefinition legacyTypeDefinition, final PersistenceTypeHandler<D, T> currentTypeHandler)
		{
			this.logger().persistenceLegacyTypeMapper_beforeEnsureLegacyTypeHandler(legacyTypeDefinition, currentTypeHandler);
			
			final PersistenceLegacyTypeHandler<D, T> legacyTypeHandler = this.wrapped().ensureLegacyTypeHandler(legacyTypeDefinition, currentTypeHandler);
			
			this.logger().persistenceLegacyTypeMapper_afterEnsureLegacyTypeHandler(legacyTypeDefinition, legacyTypeHandler);
			
			return legacyTypeHandler;
		}
		
	}
	
}
