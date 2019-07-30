package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.collections.XUtilsCollection;
import one.microstream.collections.types.XGettingSequence;

public interface PersistenceTypeDefinitionMemberCreator
{
	public PersistenceTypeDefinitionMemberPrimitiveDefinition createDefinitionMember(
		PersistenceTypeDescriptionMemberPrimitiveDefinition description
	);
	
	public PersistenceTypeDefinitionMemberEnumConstant createDefinitionMember(
		PersistenceTypeDescriptionMemberEnumConstant description
	);
	
	public PersistenceTypeDefinitionMemberFieldReflective createDefinitionMember(
		PersistenceTypeDescriptionMemberFieldReflective description
	);
	
	public PersistenceTypeDefinitionMemberFieldGenericSimple createDefinitionMember(
		PersistenceTypeDescriptionMemberFieldGenericSimple description
	);
	
	public PersistenceTypeDefinitionMemberFieldGenericVariableLength createDefinitionMember(
		PersistenceTypeDescriptionMemberFieldGenericVariableLength description
	);
	
	public PersistenceTypeDefinitionMemberFieldGenericComplex createDefinitionMember(
		PersistenceTypeDescriptionMemberFieldGenericComplex description
	);
	
	
	
	public static PersistenceTypeDefinitionMemberCreator.Default New(
		final XGettingSequence<? extends PersistenceTypeDescription> ascendingOrderTypeIdEntries,
		final PersistenceTypeResolver                                typeResolver
	)
	{
		return new PersistenceTypeDefinitionMemberCreator.Default(
			XUtilsCollection.toArray(ascendingOrderTypeIdEntries, PersistenceTypeDescription.class),
			notNull(typeResolver)
		);
	}
	
	public final class Default implements PersistenceTypeDefinitionMemberCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeDescription[] ascendingOrderTypeIdEntries;
		private final PersistenceTypeResolver      typeResolver               ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
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
		
		@Override
		public PersistenceTypeDefinitionMemberEnumConstant createDefinitionMember(
			final PersistenceTypeDescriptionMemberEnumConstant description
		)
		{
			return PersistenceTypeDefinitionMemberEnumConstant.New(description.name());
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
		public PersistenceTypeDefinitionMemberFieldReflective createDefinitionMember(
			final PersistenceTypeDescriptionMemberFieldReflective description
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
			
			return PersistenceTypeDefinitionMemberFieldReflective.New(
				runtimeDeclaringType                 ,
				field == null
					? null
					: field.getDeclaringClass()      ,
				field                                ,
				currentType                          ,
				description.typeName()               ,
				description.name()                   ,
				description.declaringTypeName()      ,
				description.isReference()            ,
				description.persistentMinimumLength(),
				description.persistentMaximumLength()
			);
		}

		@Override
		public PersistenceTypeDefinitionMemberFieldGenericSimple createDefinitionMember(
			final PersistenceTypeDescriptionMemberFieldGenericSimple description
		)
		{
			final Class<?> currentType = this.tryResolveCurrentType(description.typeName());
			
			return PersistenceTypeDefinitionMemberFieldGenericSimple.New(
				description.typeName()               ,
				description.qualifier()              ,
				description.name()                   ,
				currentType                          ,
				description.isReference()            ,
				description.persistentMinimumLength(),
				description.persistentMaximumLength()
			);
		}

		@Override
		public PersistenceTypeDefinitionMemberFieldGenericVariableLength createDefinitionMember(
			final PersistenceTypeDescriptionMemberFieldGenericVariableLength description
		)
		{
			return PersistenceTypeDefinitionMemberFieldGenericVariableLength.New(description);
		}
		
		@Override
		public PersistenceTypeDefinitionMemberFieldGenericComplex createDefinitionMember(
			final PersistenceTypeDescriptionMemberFieldGenericComplex description
		)
		{
			return PersistenceTypeDefinitionMemberFieldGenericComplex.New(description);
		}
		
	}
	
}
