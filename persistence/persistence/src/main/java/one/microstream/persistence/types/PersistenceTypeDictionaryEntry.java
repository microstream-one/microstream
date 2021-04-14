package one.microstream.persistence.types;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;

/**
 * Data that ties a {@link PersistenceTypeDescription} to a biunique type id, aka a {@link PersistenceTypeIdentity}.
 * 
 * 
 *
 */
public interface PersistenceTypeDictionaryEntry extends PersistenceTypeDescription
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	@Override
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceMembers();

	
	
	public abstract class Abstract implements PersistenceTypeDictionaryEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String toString()
		{
			return PersistenceTypeDictionaryAssembler.New()
				.assembleTypeDescription(VarString.New(), this)
				.toString()
			;
		}

	}
	
}
