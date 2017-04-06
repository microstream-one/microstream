package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;

@FunctionalInterface
public interface PersistenceTypeDescriptionBuilder
{
	public <T> PersistenceTypeDescription<T> build(
		long                                                         typeId  ,
		String                                                       typeName,
		Class<T>                                                     type    ,
		XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	);
	
	
	
	public static PersistenceTypeDescriptionBuilder New(final PersistenceTypeResolver typeResolver)
	{
		return new TypeResolving(
			notNull(typeResolver)
		);
	}
	
	public final class TypeResolving implements PersistenceTypeDescriptionBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeResolver typeResolver;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		TypeResolving(final PersistenceTypeResolver typeResolver)
		{
			super();
			this.typeResolver = typeResolver;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> PersistenceTypeDescription<T> build(
			final long                                                         typeId  ,
			final String                                                       typeName,
			final Class<T>                                                     type    ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			@SuppressWarnings("unchecked")
			final Class<T> effectiveType = (Class<T>)this.typeResolver.resolveType(typeName);
			
			if(type != null && type != effectiveType)
			{
				// (06.04.2017 TM)TODO: better suited exception
				throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(typeName);
			}
			
			return PersistenceTypeDescription.New(typeId, typeName, effectiveType, members.immure());
			
		}
		
	}
	
}