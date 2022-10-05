package one.microstream.persistence.types;

/*-
 * #%L
 * MicroStream Persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import org.slf4j.Logger;

import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeConsistencyEnum;
import one.microstream.reflect.XReflect;
import one.microstream.util.logging.Logging;
import one.microstream.util.similarity.Similarity;

public interface PersistenceLegacyTypeHandlerCreator<D>
{
	public <T> PersistenceLegacyTypeHandler<D, T> createLegacyTypeHandler(
		PersistenceLegacyTypeMappingResult<D, T> mappingResult
	);
		
	
	
	public abstract class Abstract<D> implements PersistenceLegacyTypeHandlerCreator<D>
	{
		private final static Logger logger = Logging.getLogger(Abstract.class);
		
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
						//allow ordinal changes only by explicit manual mappings
						if(targetOrdinal != ordinal)
						{
							if(match.similarity() != PersistenceLegacyTypeMapper.Defaults.defaultExplicitMappingSimilarity())
							{
								throw new PersistenceExceptionTypeConsistencyEnum(
									targetCurrentConstant.identifier(),
									result.currentTypeHandler().typeName(),
									ordinal,
									targetOrdinal
								);
							}
						}
								
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
				final PersistenceLegacyTypeHandler<D, T> reflectiveHandler = this.deriveReflectiveHandler(
					result,
					(PersistenceTypeHandlerReflective<D, T>)result.currentTypeHandler()
				);
				
				this.logHandlerCreation("reflective", reflectiveHandler);
				
				return reflectiveHandler;
			}

			final PersistenceLegacyTypeHandler<D, T> customWrappingHandler = this.deriveCustomWrappingHandler(result);
			
			this.logHandlerCreation("custom wrapping", customWrappingHandler);
			
			return customWrappingHandler;
		}
		
		private void logHandlerCreation(final String handlerType, final PersistenceLegacyTypeHandler<?, ?> handler)
		{
			logger.debug(
				"Create {} legacy type handler for {}: {}",
				handlerType,
				handler.type().getName(),
				XChars.systemString(handler)
			);
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
