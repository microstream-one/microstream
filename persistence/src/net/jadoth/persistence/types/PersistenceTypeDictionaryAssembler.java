package net.jadoth.persistence.types;

import net.jadoth.chars.ObjectStringAssembler;
import net.jadoth.chars.VarString;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.internal.TypeDictionaryAppenderBuilder;
import net.jadoth.persistence.internal.TypeDictionaryAppenderImplementation;

public interface PersistenceTypeDictionaryAssembler extends ObjectStringAssembler<PersistenceTypeDictionary>
{
	@Override
	public VarString assemble(VarString vc, PersistenceTypeDictionary typeDictionary);

	public VarString assembleTypeDescription(VarString vc, PersistenceTypeDefinition typeDescription);


	
	
	public static PersistenceTypeDictionaryAssembler New()
	{
		return new PersistenceTypeDictionaryAssembler.Implementation();
	}

	public class Implementation
	extends PersistenceTypeDictionary.Symbols
	implements PersistenceTypeDictionaryAssembler
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final int  MAX_LONG_LENGTH = 19 ;
		private static final char ID_PADDING_CHAR = '0';
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected final VarString appendPaddedId(final VarString vc, final long id)
		{
			return vc.padLeft(Long.toString(id), MAX_LONG_LENGTH, ID_PADDING_CHAR);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public VarString assemble(final VarString vc, final PersistenceTypeDictionary typeDictionary)
		{
			for(final PersistenceTypeDefinition td : typeDictionary.allTypeDefinitions().values())
			{
				this.assembleTypeDescription(vc, td);
			}
			return vc;
		}

		private static TypeDictionaryAppenderBuilder appenderBuilder(final VarString vc)
		{
			return new TypeDictionaryAppenderBuilder(vc, 1);
		}

		private void appendTypeDictionaryMembers(
			final VarString vc,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> typeMembers
		)
		{
			if(typeMembers.isEmpty())
			{
				return;
			}
			final TypeDictionaryAppenderImplementation appender = typeMembers.iterate(appenderBuilder(vc.lf())).yield();
			typeMembers.iterate(appender);
		}

		@Override
		public VarString assembleTypeDescription(final VarString vc, final PersistenceTypeDefinition typeDescription)
		{
			this.appendTypeDefinitionStart  (vc, typeDescription);
			this.appendTypeDictionaryMembers(vc, typeDescription.members());
			this.appendTypeDefinitionEnd    (vc, typeDescription);
			return vc;
		}

		protected void appendTypeDefinitionStart(final VarString vc, final PersistenceTypeDefinition typeDescription)
		{
			this.appendPaddedId(vc, typeDescription.typeId())
				.blank().add(typeDescription.typeName())
				.append(TYPE_START)
			;
		}

		protected void appendTypeDefinitionEnd(final VarString vc, final PersistenceTypeDefinition typeDescription)
		{
			vc.append(TYPE_END).lf();
		}

	}

}
