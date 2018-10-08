package net.jadoth.persistence.types;


public interface PersistenceTypeDefinitionMember<O> extends PersistenceTypeDescriptionMember
{
	/**
	 * The {@link PersistenceTypeDefinition} that owns this member.
	 * Note: this is not necessarily the type definition of the declaring type.
	 * @return
	 */
	public PersistenceTypeDefinition<O> ownerType();
	
	/**
	 * The runtime type used by this description member, if possible. Otherwise <code>null</code>.
	 * 
	 * @return
	 */
	public Class<?> type();

	
	
	public interface EffectiveFinalOwnerTypeHolder<O> extends PersistenceTypeDefinitionMember<O>
	{
		public default void initializeOwnerType(final PersistenceTypeDefinition<O> ownerType)
		{
			synchronized(this)
			{
				if(this.ownerType() != null)
				{
					if(this.ownerType() == ownerType)
					{
						return;
					}
					
					// (08.10.2018 TM)EXCP: proper exception
					throw new RuntimeException("Owner type already initialized.");
				}
				
				this.internalSetValidatedOwnerType(ownerType);
			}
			
		}
		
		public void internalSetValidatedOwnerType(PersistenceTypeDefinition<O> ownerType);
	}

}
