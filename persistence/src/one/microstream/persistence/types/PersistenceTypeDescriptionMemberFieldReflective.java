package one.microstream.persistence.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface PersistenceTypeDescriptionMemberFieldReflective
extends PersistenceTypeDescriptionMemberField
{
	@Override
	public String identifier();
	
	public default String declaringTypeName()
	{
		return this.qualifier();
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberFieldReflective createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}
	

	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldReflective m1,
		final PersistenceTypeDescriptionMemberFieldReflective m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldReflective m1,
		final PersistenceTypeDescriptionMemberFieldReflective m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	

	// (14.08.2015 TM)TODO: include Generics, Field#getGenericType
//	public String typeParameterString();
	
	
	
	public static PersistenceTypeDescriptionMemberFieldReflective New(
		final String  typeName               ,
		final String  declaringTypeName      ,
		final String  name                   ,
		final boolean isReference            ,
		final long    persistentMinimumLength,
		final long    persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldReflective.Default(
			 notNull(typeName)               ,
			 notNull(declaringTypeName)      ,
			 notNull(name)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public class Default
	extends PersistenceTypeDescriptionMemberField.Abstract
	implements PersistenceTypeDescriptionMemberFieldReflective
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String  typeName           ,
			final String  declaringTypeName  ,
			final String  name               ,
			final boolean isReference        ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(
				typeName           ,
				declaringTypeName  ,
				name               ,
				isReference        ,
				!isReference       ,
				isReference        ,
				persistentMinLength,
				persistentMaxLength
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}
		
	}

}
