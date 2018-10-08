package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.XUtilsCollection;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.reflect.XReflect;

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
		
		private PersistenceTypeDescription determineLatestType(final String typeName)
		{
			final PersistenceTypeDescription[] ascendingOrderTypeIdEntries = this.ascendingOrderTypeIdEntries;
			for(int i = ascendingOrderTypeIdEntries.length; i --> 0;)
			{
				if(typeName.equals(ascendingOrderTypeIdEntries[i].typeName()))
				{
					return ascendingOrderTypeIdEntries[i];
				}
			}
			
			return null;
		}
		
		private Class<?> resolveCurrentType(final String typeName)
		{
			final PersistenceTypeDescription latestType = this.determineLatestType(typeName);
			final String effectiveLatestTypeName = this.typeResolver.resolveRuntimeTypeName(latestType);
			
			return effectiveLatestTypeName == null
				? null
				: this.typeResolver.resolveType(effectiveLatestTypeName)
			;
		}
		
		private String resolveRuntimeTypeName(final String typeName)
		{
			final PersistenceTypeDescription latestDeclaringType = this.determineLatestType(typeName);
			final String runtimeTypeName = this.typeResolver.resolveRuntimeTypeName(latestDeclaringType);
			
			return runtimeTypeName;
		}
		
		private Field resolveField(final String declaringClassName, final String fieldName)
		{
			final Class<?> declaringClass = this.typeResolver.resolveType(declaringClassName);
			final Field field = XReflect.getDeclaredField(declaringClass, fieldName);
			
			return field;
		}
		
		@Override
		public PersistenceTypeDefinitionMemberField createDefinitionMember(
			final PersistenceTypeDescriptionMemberField description
		)
		{
			final Class<?> currentType = this.resolveCurrentType(description.typeName());
			
			final String runtimeDeclaringType = this.resolveRuntimeTypeName(description.declaringTypeName());

			// if the declaring type does not stay the same, there is no sense in resolving a field.
			final Field field = description.declaringTypeName().equals(runtimeDeclaringType)
				? this.resolveField(runtimeDeclaringType, description.name())
				: null
			;
			
			return PersistenceTypeDefinitionMemberField.New(
				field == null ? null : field.getDeclaringClass(),
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
			final Class<?> currentType = this.resolveCurrentType(description.typeName());
			
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
