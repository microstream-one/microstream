package one.microstream.persistence.types;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;

/**
 * Data that ties a {@link PersistenceTypeDescription} to a biunique type id, aka a {@link PersistenceTypeIdentity}.
 * 
 * @author TM
 *
 */
public interface PersistenceTypeDictionaryEntry extends PersistenceTypeDescription
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	@Override
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> members();

	
	
	/* (05.10.2018 TM)TODO: Type Dictionary: consolidate PersistenceTypeDictionaryEntry assembling
	 * The assembling should not be here and not with hard-coded meta characters.
	 */
	public static VarString assembleDictionaryString(final VarString vs, final PersistenceTypeDictionaryEntry e)
	{
		vs.add(e.typeId()).blank().add(e.typeName()).blank().add('{');
		if(!e.members().isEmpty())
		{
			vs.lf();
			for(final PersistenceTypeDescriptionMember member : e.members())
			{
				vs.tab().add(member).add(';').lf();
			}
		}
		vs.add('}');
		
		return vs;
	}
	
	public static String assembleDictionaryString(final PersistenceTypeDictionaryEntry e)
	{
		return assembleDictionaryString(VarString.New(), e).toString();
	}
	
	public abstract class AbstractImplementation implements PersistenceTypeDictionaryEntry
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String toString()
		{
			return PersistenceTypeDictionaryEntry.assembleDictionaryString(this);
		}

	}
	
}
