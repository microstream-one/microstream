package one.microstream.persistence.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface PersistenceTypeDescriptionMemberFieldReflective extends PersistenceTypeDescriptionMember
{
	@Override
	public String uniqueName();
	

	// (14.08.2015 TM)TODO: include Generics, Field#getGenericType
//	public String typeParameterString();


	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		return member instanceof PersistenceTypeDescriptionMemberFieldReflective
			&& equalDescription(this, (PersistenceTypeDescriptionMemberFieldReflective)member)
		;
	}
	
	// (09.07.2019 TM)FIXME: MS-156: check all description comparisons for type checks
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldReflective m1,
		final PersistenceTypeDescriptionMemberFieldReflective m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2)
			&& m1.qualifier().equals(m2.qualifier())
		;
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberField createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}
	
	
	
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
	extends PersistenceTypeDescriptionMember.Abstract
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
				false              ,
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
