package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.XUtilsCollection;
import net.jadoth.collections.types.XGettingSequence;

public interface PersistenceTypeDefinitionMemberCreator
{
	public PersistenceTypeDefinitionMemberPrimitiveDefinition createDefinitionMember(
		PersistenceTypeDescriptionMemberPrimitiveDefinition description
	);
	
	public PersistenceTypeDefinitionMemberField createDefinitionMember(
		PersistenceTypeDescriptionMemberField description
	);
	
	public PersistenceTypeDefinitionMemberPseudoFieldSimple createDefinitionMember(
		PersistenceTypeDescriptionMemberPseudoFieldSimple description
	);
	
	public PersistenceTypeDefinitionMemberPseudoFieldVariableLength createDefinitionMember(
		PersistenceTypeDescriptionMemberPseudoFieldVariableLength description
	);
	
	public PersistenceTypeDefinitionMemberPseudoFieldComplex createDefinitionMember(
		PersistenceTypeDescriptionMemberPseudoFieldComplex description
	);
	
	
	
	public static PersistenceTypeDefinitionMemberCreator.Implementation New(
		final XGettingSequence<? extends PersistenceTypeDescription> ascendingOrderTypeIdEntries,
		final PersistenceTypeResolver                                typeResolver
	)
	{
		return new PersistenceTypeDefinitionMemberCreator.Implementation(
			XUtilsCollection.toArray(ascendingOrderTypeIdEntries, PersistenceTypeDescription.class),
			notNull(typeResolver)
		);
	}
	
	public final class Implementation implements PersistenceTypeDefinitionMemberCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDescription[] ascendingOrderTypeIdEntries;
		private final PersistenceTypeResolver      typeResolver               ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDescription[] ascendingOrderTypeIdEntries,
			final PersistenceTypeResolver      typeResolver
		)
		{
			super();
			this.ascendingOrderTypeIdEntries = ascendingOrderTypeIdEntries;
			this.typeResolver                = typeResolver               ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDefinitionMemberPrimitiveDefinition createDefinitionMember(
			final PersistenceTypeDescriptionMemberPrimitiveDefinition description
		)
		{
			return PersistenceTypeDefinitionMemberPrimitiveDefinition.New(description);
		}
		
		private PersistenceTypeDescription determineLatestTypeEntry(final String typeName)
		{
			final PersistenceTypeDescription[] ascendingOrderTypeIdEntries = this.ascendingOrderTypeIdEntries;
			for(int i = ascendingOrderTypeIdEntries.length; i --> 0;)
			{
				if(typeName.equals(ascendingOrderTypeIdEntries[i].typeName()))
				{
					return ascendingOrderTypeIdEntries[i];
				}
			}
			
			/*
			 * Can / may only happen if the type name is an interface
			 * Interfaces can never have fields or instances, so they are not registered in the type dictionary.
			 */
			return null;
		}
		
		private Class<?> tryResolveCurrentType(final String typeName)
		{
			// can be null for types explicitly marked as no more having a runtime type (unreachable / "deleted")
			final String effectiveLatestTypeName = this.resolveRuntimeTypeName(typeName);
			
			return effectiveLatestTypeName == null
				? null
				: this.typeResolver.tryResolveType(effectiveLatestTypeName)
			;
		}
		
		private String resolveRuntimeTypeName(final String typeName)
		{
			// can be null for interface types, in which case the typeName is implicitely the effective one.
			final PersistenceTypeDescription latestTypeEntry = this.determineLatestTypeEntry(typeName);
						
			// can be null for types explicitly marked as no more having a runtime type (unreachable / "deleted")
			final String runtimeTypeName = latestTypeEntry == null
				? this.typeResolver.resolveRuntimeTypeName(typeName)
				: this.typeResolver.resolveRuntimeTypeName(latestTypeEntry)
			;
			
			return runtimeTypeName;
		}
		
		private Field resolveField(final String declaringClassName, final String fieldName)
		{
			final Class<?> declaringClass = this.typeResolver.tryResolveType(declaringClassName);
			if(declaringClass == null)
			{
				// declaring class name might no longer be resolvable
				return null;
			}
			
			final Field field;
			try
			{
				field = declaringClass.getDeclaredField(fieldName);
				return field;
			}
			catch(final NoSuchFieldException e)
			{
				// field name might no longer be resolvable
				return null;
			}
		}
		
		@Override
		public PersistenceTypeDefinitionMemberField createDefinitionMember(
			final PersistenceTypeDescriptionMemberField description
		)
		{
			final Class<?> currentType = this.tryResolveCurrentType(description.typeName());
			
			// can be null for types explicitly marked as no more having a runtime type (unreachable / "deleted")
			final String runtimeDeclaringType = this.resolveRuntimeTypeName(description.declaringTypeName());

			/*
			 * if the declaring type does not stay the same, there is no sense in resolving a field.
			 * There might have been created a class with equal name and even a field with equal name in the meantime,
			 * but with a different meaning in the design. It would be logically wrong to resolve the old definition
			 * member to that current type field.
			 */
			final Field field = description.declaringTypeName().equals(runtimeDeclaringType)
				? this.resolveField(runtimeDeclaringType, description.name())
				: null
			;
			
			return PersistenceTypeDefinitionMemberField.New(
				field == null
					? null
					: field.getDeclaringClass()      ,
				field                                ,
				currentType                          ,
				description.typeName()               ,
				description.name()                   ,
				runtimeDeclaringType                 , // necessary to have comparability in similarity calculations
				description.isReference()            ,
				description.persistentMinimumLength(),
				description.persistentMaximumLength()
			);
		}

		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldSimple createDefinitionMember(
			final PersistenceTypeDescriptionMemberPseudoFieldSimple description
		)
		{
			final Class<?> currentType = this.tryResolveCurrentType(description.typeName());
			
			return PersistenceTypeDefinitionMemberPseudoFieldSimple.New(
				description.name()                   ,
				description.typeName()               ,
				currentType                          ,
				description.isReference()            ,
				description.persistentMinimumLength(),
				description.persistentMaximumLength()
			);
		}

		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldVariableLength createDefinitionMember(
			final PersistenceTypeDescriptionMemberPseudoFieldVariableLength description
		)
		{
			return PersistenceTypeDefinitionMemberPseudoFieldVariableLength.New(description);
		}
		
		@Override
		public PersistenceTypeDefinitionMemberPseudoFieldComplex createDefinitionMember(
			final PersistenceTypeDescriptionMemberPseudoFieldComplex description
		)
		{
			return PersistenceTypeDefinitionMemberPseudoFieldComplex.New(description);
		}
		
	}
	
}
