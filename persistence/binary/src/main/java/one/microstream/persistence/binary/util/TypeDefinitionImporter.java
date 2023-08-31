package one.microstream.persistence.binary.util;

/*-
 * #%L
 * MicroStream Persistence Binary
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

import one.microstream.collections.types.XGettingSequence;
import one.microstream.equality.Equalator;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceExceptionTypeImportTypeNotFound;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerEnsurer;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.util.logging.Logging;

/**
 * Import {@link PersistenceTypeDefinition} into the supplied
 * {@link PersistenceTypeHandlerManager}.
 *
 */
public interface TypeDefinitionImporter
{
	public void importTypeDefinition(PersistenceTypeDefinition typeDefinition);
	
	void importTypeDefinitions(XGettingSequence<PersistenceTypeDefinition> typeDefinitions);

	public static class Default implements TypeDefinitionImporter
	{
		private final static Logger logger = Logging.getLogger(Default.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeHandlerManager<Binary> typeHandlerManager;
		private final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(
			final PersistenceTypeHandlerManager<Binary> typeHandlerManager,
			final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer
		)
		{
			super();
			this.typeHandlerManager = typeHandlerManager;
			this.typeHandlerEnsurer = typeHandlerEnsurer;
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void importTypeDefinition(final PersistenceTypeDefinition typeDefinition)
		{
			if(typeDefinition.type() != null)
			{
				final PersistenceTypeHandler<Binary, ?> handler = this.typeHandlerManager.lookupTypeHandler(typeDefinition.type());
				
				if(handler != null)
				{
					if(PersistenceTypeDescriptionMember.equalMembers(typeDefinition.allMembers(), handler.allMembers(), this.memberValidator))
					{
						logger.trace("Handler for type {}, typeId {} already registered.", typeDefinition.type(), typeDefinition.typeId());
					}
					else
					{
						logger.trace("Trying to create legacy type handler for type {}, typeId {}.", typeDefinition.type(), typeDefinition.typeId());
						this.typeHandlerManager.updateCurrentHighestTypeId(typeDefinition.typeId());
						this.typeHandlerManager.ensureLegacyTypeHandler(typeDefinition, handler);
					}
				}
				else
				{
					final PersistenceTypeHandler<Binary, ?> th = this.typeHandlerEnsurer.ensureTypeHandler(typeDefinition.type());
									
					if(PersistenceTypeDescriptionMember.equalMembers(typeDefinition.allMembers(), th.allMembers(), this.memberValidator))
					{
						logger.trace("Trying to create type handler for new type {}, typeId {}.", typeDefinition.type(), typeDefinition.typeId());
						this.typeHandlerManager.ensureTypeHandler(typeDefinition.type());
					}
					else
					{
						logger.trace("Trying to create legacy type handler for new type {}, typeId {}.", typeDefinition.type(), typeDefinition.typeId());
						this.typeHandlerManager.updateCurrentHighestTypeId(typeDefinition.typeId());
						this.typeHandlerManager.ensureLegacyTypeHandler(typeDefinition, th);
					}
				}
				
			}
			else
			{
				logger.error("Failed to resolve new type {}.", typeDefinition.typeName());
				throw new BinaryPersistenceExceptionTypeImportTypeNotFound(typeDefinition.typeName());
			}
			
		}
		
		@Override
		public void importTypeDefinitions(final XGettingSequence<PersistenceTypeDefinition> typeDefinitions)
		{
			for (final PersistenceTypeDefinition typeDefinition : typeDefinitions)
			{
				this.importTypeDefinition(typeDefinition);
			}
		}
							
		private final Equalator<PersistenceTypeDescriptionMember> memberValidator = (m1, m2) ->
		{
			if(m1 == null || m2 == null)
			{
				return false;
			}

			return m1.equalsStructure(m2);
		};
	}
}
