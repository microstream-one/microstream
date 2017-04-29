package net.jadoth.persistence.binary.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceCustomTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceTypeHandlerEnsurerLookup;
import net.jadoth.persistence.types.PersistenceTypeHandlerEnsurer;
import net.jadoth.persistence.types.PersistenceTypeHandlerLookup;

public interface BinaryTypeHandlerEnsurerLookup extends PersistenceTypeHandlerEnsurerLookup<Binary>
{
	@Override
	public PersistenceTypeHandlerEnsurer<Binary> lookupEnsurer(Class<?> type)
		throws PersistenceExceptionTypeNotPersistable;



	public class Implementation implements BinaryTypeHandlerEnsurerLookup
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeHandlerEnsurer<Binary>        genericTypeHandlerCreator;
		private final PersistenceCustomTypeHandlerRegistry<Binary> customTypeHandlerRegistry;
//		private final BinaryHandlerNativeClass                     classTypeHandler         ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceTypeHandlerEnsurer<Binary>        genericTypeHandlerCreator,
			final PersistenceCustomTypeHandlerRegistry<Binary> customTypeHandlerRegistry,
			final PersistenceTypeHandlerLookup<Binary>         typeHandlerLookup
		)
		{
			super();
			this.genericTypeHandlerCreator = notNull(genericTypeHandlerCreator)                     ;
			this.customTypeHandlerRegistry = notNull(customTypeHandlerRegistry)                     ;
//			this.classTypeHandlerCreator   = new BinaryHandlerNativeClass.Creator(typeHandlerLookup);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public PersistenceTypeHandlerEnsurer<Binary> lookupEnsurer(final Class<?> type)
			throws PersistenceExceptionTypeNotPersistable
		{
//			// special case Class type handler (must be checked before custom types because of reflective type unsafety)
//			if(type == Class.class)
//			{
//				return this.classTypeHandlerCreator;
//			}

			// check for registered custom type handling (type Class can never be relevant, even if registered)
			if(this.customTypeHandlerRegistry.knowsType(type))
			{
				return this.customTypeHandlerRegistry;
			}

			// generic implementations handles all other cases (primitives, arrays and standard by-field-analyzers)
			return this.genericTypeHandlerCreator;
		}

	}

}
