package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDescriptionMismatchListener
{
	public void registerMismatch(
		PersistenceTypeDescription<?> latestPersistedDescription,
		PersistenceTypeDescription<?> runtimeDescription
	);
	
	
	
	public static PersistenceTypeDescriptionMismatchListener.Implementation New()
	{
		return new PersistenceTypeDescriptionMismatchListener.Implementation();
	}
	
	public final class Implementation implements PersistenceTypeDescriptionMismatchListener
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void registerMismatch(
			final PersistenceTypeDescription<?> latestPersistedDescription,
			final PersistenceTypeDescription<?> runtimeDescription
		)
		{
			/* (28.04.2017 TM)NOTE:
			 * the default implementation simply throws an exception
			 * without further logic to compensate.
			 */
			
			// (28.04.2017 TM)EXCP: proper exception
			throw new RuntimeException(
				PersistenceTypeDescription.class.getSimpleName()
				+ " inconsistency between runtime and latest persisted for type "
				+ latestPersistedDescription.typeName()
			);
		}
		
	}
}
