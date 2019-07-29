package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.Objects;

public interface PersistenceTypeDescriptionMemberEnumConstant extends PersistenceTypeDescriptionMember
{
	public String enumRuntimeName();
	
	public boolean isDeleted();

	
	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember member)
	{
		return member instanceof PersistenceTypeDescriptionMemberEnumConstant
			&& equalDescription(this, (PersistenceTypeDescriptionMemberEnumConstant)member)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberEnumConstant m1,
		final PersistenceTypeDescriptionMemberEnumConstant m2
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

	
	public static PersistenceTypeDescriptionMemberEnumConstant New(
		final String  enumPersistentName,
		final String  enumRuntimeName   ,
		final boolean isDeleted
	)
	{
		return new PersistenceTypeDescriptionMemberEnumConstant.Default(
			 notNull(enumPersistentName),
			 notNull(enumRuntimeName   ),
			         isDeleted
		);
	}

	public class Default
	implements PersistenceTypeDescriptionMemberEnumConstant
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String  enumPersistentName;
		private final String  enumRuntimeName   ;
		private final boolean isDeleted         ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String  enumPersistentName,
			final String  enumRuntimeName   ,
			final boolean isDeleted
		)
		{
			super();
			this.enumPersistentName = notNull(enumPersistentName);
			this.enumRuntimeName    = notNull(enumRuntimeName   );
			this.isDeleted          =         isDeleted          ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String name()
		{
			return this.enumPersistentName;
		}

		@Override
		public String enumRuntimeName()
		{
			return this.enumRuntimeName;
		}
		
		@Override
		public boolean isDeleted()
		{
			return this.isDeleted;
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}
		
		@Override
		public long persistentMinimumLength()
		{
			return this.persistentLength;
		}
		
		@Override
		public long persistentMaximumLength()
		{
			return this.persistentLength;
		}

		@Override
		public boolean isValidPersistentLength(final long persistentLength)
		{
			return persistentLength == this.persistentLength;
		}
		
		@Override
		public boolean equalsStructure(final PersistenceTypeDescriptionMember other)
		{
			// the check for equal (namely null) typename and name is still valid here.
			return PersistenceTypeDescriptionMemberEnumConstant.super.equalsStructure(other)
				&& other instanceof PersistenceTypeDescriptionMemberEnumConstant
				&& Objects.equals(
					this.primitiveDefinition(),
					((PersistenceTypeDescriptionMemberEnumConstant)other).primitiveDefinition()
				)
			;
		}

		@Override
		public String typeName()
		{
			return null;
		}
		
		@Override
		public String qualifier()
		{
			return null;
		}

		@Override
		public final boolean isReference()
		{
			return false;
		}

		@Override
		public final boolean isPrimitive()
		{
			return false;
		}

		@Override
		public final boolean isPrimitiveDefinition()
		{
			return true;
		}

		@Override
		public final boolean hasReferences()
		{
			return false;
		}

		@Override
		public void validatePersistentLength(final long persistentLength)
		{
			if(this.isValidPersistentLength(persistentLength))
			{
				return;
			}
			// (02.05.2014)EXCP: proper exception
			throw new RuntimeException(
				"Invalid persistent length: " + persistentLength
				+ " != " + this.persistentLength + "."
			);
		}

	}

}
