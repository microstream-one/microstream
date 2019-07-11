package one.microstream.persistence.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import java.util.Objects;

public interface PersistenceTypeDescriptionMemberPrimitiveDefinition extends PersistenceTypeDescriptionMember
{
	public String primitiveDefinition();
	
	@Override
	public default String identifier()
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

	
	public static PersistenceTypeDescriptionMemberPrimitiveDefinition New(
		final String primitiveDefinition    ,
		final long   persistentMinimumLength,
		final long   persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberPrimitiveDefinition.Default(
			 notNull(primitiveDefinition)    ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMember.Abstract
	implements PersistenceTypeDescriptionMemberPrimitiveDefinition
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String primitiveDefinition;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String primitiveDefinition    ,
			final long   persistentMinimumLength,
			final long   persistentMaximumLength
		)
		{
			super(null, null, null, false, false, true, false, persistentMinimumLength, persistentMaximumLength);
			this.primitiveDefinition = notNull(primitiveDefinition);
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
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}
		
		@Override
		public boolean equalsStructure(final PersistenceTypeDescriptionMember other)
		{
			// the check for equal (namely null) typename and name is still valid here.
			return super.equalsStructure(other)
				&& other instanceof PersistenceTypeDescriptionMemberPrimitiveDefinition
				&& Objects.equals(
					this.primitiveDefinition(),
					((PersistenceTypeDescriptionMemberPrimitiveDefinition)other).primitiveDefinition()
				)
			;
		}

	}

}
