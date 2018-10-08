package net.jadoth.persistence.types;

import net.jadoth.chars.VarString;



public interface PersistenceTypeDefinitionMemberPrimitiveDefinition<O>
extends PersistenceTypeDescriptionMemberPrimitiveDefinition, PersistenceTypeDefinitionMember<O>
{


	public final class Implementation<O>
	extends PersistenceTypeDescriptionMemberPrimitiveDefinition.Implementation
	implements
	PersistenceTypeDefinitionMemberPrimitiveDefinition<O>,
	PersistenceTypeDefinitionMember.EffectiveFinalOwnerTypeHolder<O>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// owner type must be initialized effectively final to prevent circular constructor dependencies
		private /*f*/ PersistenceTypeDefinition<O> ownerType;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final Class<?> primitiveType          ,
			final long     persistentMinimumLength,
			final long     persistentMaximumLength
		)
		{
			this(
				assemblePrimitiveDefinition(VarString.New(), primitiveType).toString(),
				persistentMinimumLength,
				persistentMaximumLength
			);
		}

		public Implementation(
			final String primitiveDefinition    ,
			final long   persistentMinimumLength,
			final long   persistentMaximumLength
		)
		{
			super(primitiveDefinition, persistentMinimumLength, persistentMaximumLength);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final PersistenceTypeDefinition<O> ownerType()
		{
			return this.ownerType;
		}
		
		@Override
		public final void internalSetValidatedOwnerType(final PersistenceTypeDefinition<O> ownerType)
		{
			this.ownerType = ownerType;
		}
		
		@Override
		public final Class<?> type()
		{
			// a definition does not have a member / field type. The defined primitive type is in the owner type.
			return null;
		}
		
	}

}
