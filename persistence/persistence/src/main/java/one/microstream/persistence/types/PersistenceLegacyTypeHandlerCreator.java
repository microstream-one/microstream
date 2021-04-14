package one.microstream.persistence.types;

import one.microstream.collections.BulkList;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.reflect.XReflect;
import one.microstream.util.similarity.Similarity;

public interface PersistenceLegacyTypeHandlerCreator<D>
{
	public <T> PersistenceLegacyTypeHandler<D, T> createLegacyTypeHandler(
		PersistenceLegacyTypeMappingResult<D, T> mappingResult
	);
		
	
	
	public abstract class Abstract<D> implements PersistenceLegacyTypeHandlerCreator<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static Integer[] deriveEnumOrdinalMapping(final PersistenceLegacyTypeMappingResult<?, ?> result)
		{
			final PersistenceTypeDefinition legacyTypeDef = result.legacyTypeDefinition();
			final BulkList<PersistenceTypeDefinitionMember> legacyConstantMembers = legacyTypeDef.allMembers()
				.filterTo(BulkList.New(), PersistenceTypeDefinitionMember::isEnumConstant)
			;
			
			final PersistenceTypeDefinition currentTypeDef = result.currentTypeHandler();
			final BulkList<PersistenceTypeDefinitionMember> currentConstantMembers = currentTypeDef.allMembers()
				.filterTo(BulkList.New(), PersistenceTypeDefinitionMember::isEnumConstant)
			;
			
			final Integer[] ordinalMap = new Integer[legacyConstantMembers.intSize()];
			
			int ordinal = 0;
			for(final PersistenceTypeDefinitionMember legacyMember : legacyConstantMembers)
			{
				final Similarity<PersistenceTypeDefinitionMember> match =
					result.legacyToCurrentMembers().get(legacyMember)
				;
				if(match == null)
				{
					if(result.discardedLegacyMembers().contains(legacyMember))
					{
						ordinalMap[ordinal] = null;
					}
					else
					{
						throw new PersistenceException(
							"Unmapped legacy enum constant: " + legacyTypeDef + "#" + legacyMember.name()
						);
					}
				}
				else
				{
					final PersistenceTypeDefinitionMember targetCurrentConstant = match.targetElement();
					final long targetOrdinal = currentConstantMembers.indexOf(targetCurrentConstant);
					if(targetOrdinal >= 0)
					{
						ordinalMap[ordinal] = Integer.valueOf((int)targetOrdinal);
					}
					else
					{
						throw new PersistenceException(
							"Inconsistent target enum constant: " + currentTypeDef + "#" + targetCurrentConstant.name()
						);
					}
				}
				ordinal++;
			}
			
			return ordinalMap;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public <T> PersistenceLegacyTypeHandler<D, T> createLegacyTypeHandler(
			final PersistenceLegacyTypeMappingResult<D, T> result
		)
		{
			if(PersistenceLegacyTypeMappingResult.isUnchangedInstanceStructure(result))
			{
				/*
				 * special case: structure didn't change, only namings, so the current type handler can be used.
				 * Note that this applies to custom handlers, too. Even ones with variable length instances.
				 */
				return this.createTypeHandlerUnchangedInstanceStructure(result);
			}
			
			if(result.currentTypeHandler() instanceof PersistenceTypeHandlerReflective<?, ?>)
			{
				return this.deriveReflectiveHandler(
					result,
					(PersistenceTypeHandlerReflective<D, T>)result.currentTypeHandler()
				);
			}

			return this.deriveCustomWrappingHandler(result);
		}
		
		protected <T> PersistenceLegacyTypeHandler<D, T> createTypeHandlerUnchangedInstanceStructure(
			final PersistenceLegacyTypeMappingResult<D, T> result
		)
		{
			if(XReflect.isEnum(result.currentTypeHandler().type()))
			{
				return this.createTypeHandlerUnchangedInstanceStructureGenericEnum(result);
			}

			return this.createTypeHandlerUnchangedInstanceStructureGenericType(result);
		}
				
		protected <T> PersistenceLegacyTypeHandler<D, T> createTypeHandlerUnchangedInstanceStructureGenericEnum(
			final PersistenceLegacyTypeMappingResult<D, T> result
		)
		{
			return this.createTypeHandlerEnumWrapping(result, result.currentTypeHandler());
		}
		
		protected <T> PersistenceLegacyTypeHandler<D, T> createTypeHandlerEnumWrapping(
			final PersistenceLegacyTypeMappingResult<D, T> result     ,
			final PersistenceTypeHandler<D, T>             typeHandler
		)
		{
			if(PersistenceLegacyTypeMappingResult.isUnchangedStaticStructure(result))
			{
				// current enum type handler is generically wrapped
				return PersistenceLegacyTypeHandlerWrapper.New(
					result.legacyTypeDefinition(),
					typeHandler
				);
			}
			
			final Integer[] ordinalMapping = deriveEnumOrdinalMapping(result);
			
			return PersistenceLegacyTypeHandlerWrapperEnum.New(
				result.legacyTypeDefinition(),
				typeHandler,
				ordinalMapping
			);
		}
				
		protected <T> PersistenceLegacyTypeHandler<D, T> createTypeHandlerUnchangedInstanceStructureGenericType(
			final PersistenceLegacyTypeMappingResult<D, T> result
		)
		{
			return PersistenceLegacyTypeHandlerWrapper.New(
				result.legacyTypeDefinition(),
				result.currentTypeHandler()
			);
		}
							
		protected abstract <T> PersistenceLegacyTypeHandler<D, T> deriveCustomWrappingHandler(
			PersistenceLegacyTypeMappingResult<D, T> mappingResult
		);
		
		protected abstract <T> PersistenceLegacyTypeHandler<D, T> deriveReflectiveHandler(
			PersistenceLegacyTypeMappingResult<D, T> mappingResult,
			PersistenceTypeHandlerReflective<D, T>   currentTypeHandler
		);
	}
	
}
