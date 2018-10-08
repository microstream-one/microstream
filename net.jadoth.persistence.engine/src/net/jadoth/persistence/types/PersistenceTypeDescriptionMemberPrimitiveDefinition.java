package net.jadoth.persistence.types;

public interface PersistenceTypeDescriptionMemberPrimitiveDefinition extends PersistenceTypeDescriptionMember
{
	public String primitiveDefinition();
	
	@Override
	public default String uniqueName()
	{
		return this.primitiveDefinition();
	}

	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		return member instanceof PersistenceTypeDescriptionMemberPrimitiveDefinition
			&& equalDescription(this, (PersistenceTypeDescriptionMemberPrimitiveDefinition)member)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberPrimitiveDefinition m1,
		final PersistenceTypeDescriptionMemberPrimitiveDefinition m2
	)
	{
		return m1.primitiveDefinition().equals(m2.primitiveDefinition());
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberPrimitiveDefinition createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}


	public class Implementation
	extends PersistenceTypeDescriptionMember.AbstractImplementation
	implements PersistenceTypeDescriptionMemberPrimitiveDefinition
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String primitiveDefinition;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final String primitiveDefinition    ,
			final long   persistentMinimumLength,
			final long   persistentMaximumLength
		)
		{
			super(null, null, false, false, true, false, persistentMinimumLength, persistentMaximumLength);
			this.primitiveDefinition = primitiveDefinition;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String primitiveDefinition()
		{
			return this.primitiveDefinition;
		}

		@Override
		public void assembleTypeDescription(final Appender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

		@Override
		public boolean equalsDescription(final PersistenceTypeDescriptionMember member)
		{
			return member instanceof PersistenceTypeDescriptionMemberPrimitiveDefinition
				&& PersistenceTypeDescriptionMemberPrimitiveDefinition.equalDescription(
					this,
					(PersistenceTypeDescriptionMemberPrimitiveDefinition)member
				)
			;
		}

	}

}
