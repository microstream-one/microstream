package one.microstream.communication.binarydynamic;

/*-
 * #%L
 * MicroStream Communication Binary
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
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerEnsurer;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.util.logging.Logging;

public class ComHandlerReceiveMessageNewType implements ComHandlerReceive<ComMessageNewType>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private final static Logger logger = Logging.getLogger(ComHandlerReceiveMessageNewType.class);
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeHandlerManager<Binary> typeHandlerManager  ;
	private final ComTypeDefinitionBuilder              typeDefintionBuilder;
	private final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer  ;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComHandlerReceiveMessageNewType(
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager  ,
		final ComTypeDefinitionBuilder              typeDefintionBuilder,
		final PersistenceTypeHandlerEnsurer<Binary> typeHandlerEnsurer
	)
	{
		super();
		this.typeHandlerManager         = typeHandlerManager  ;
		this.typeDefintionBuilder       = typeDefintionBuilder;
		this.typeHandlerEnsurer         = typeHandlerEnsurer  ;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	final Equalator<PersistenceTypeDescriptionMember> memberValidator = (m1, m2) ->
	{
		if(m1 == null || m2 == null)
		{
			return false;
		}

		if(m1.equalsStructure(m2))
		{
			return true;
		}

		return false;
	};
	
	@Override
	public Void processMessage(final ComMessageNewType message)
	{
		final String typeEntry = message.typeEntry();
		logger.debug("received new type entry: \n {}", typeEntry);
		
		final XGettingSequence<PersistenceTypeDefinition> defs = this.typeDefintionBuilder.buildTypeDefinitions(typeEntry);
		for (final PersistenceTypeDefinition ptd : defs)
		{
			if(ptd.type() != null)
			{
				final PersistenceTypeHandler<Binary, ?> handler = this.typeHandlerManager.lookupTypeHandler(ptd.type());
				
				if(handler != null)
				{
					if(PersistenceTypeDescriptionMember.equalMembers(ptd.allMembers(), handler.allMembers(), this.memberValidator))
					{
						logger.trace("handler for type {}, typeId {} already registered",ptd.type(), ptd.typeId());
					}
					else
					{
						logger.trace("trying to create legacy type handler for type {}, typeId {}",ptd.type(), ptd.typeId());
						this.typeHandlerManager.updateCurrentHighestTypeId(ptd.typeId());
						this.typeHandlerManager.ensureLegacyTypeHandler(ptd, handler);
					}
				}
				else
				{
					final PersistenceTypeHandler<Binary, ?> th = this.typeHandlerEnsurer.ensureTypeHandler(ptd.type());
									
					if(PersistenceTypeDescriptionMember.equalMembers(ptd.allMembers(), th.allMembers(), this.memberValidator))
					{
						logger.trace("trying to create type handler for new type {}, typeId {}",ptd.type(), ptd.typeId());
						this.typeHandlerManager.ensureTypeHandler(ptd.type());
					}
					else
					{
						logger.trace("trying to create legacy type handler for new type {}, typeId {}",ptd.type(), ptd.typeId());
						this.typeHandlerManager.updateCurrentHighestTypeId(ptd.typeId());
						this.typeHandlerManager.ensureLegacyTypeHandler(ptd, th);
					}
				}
			}
			else
			{
				logger.error("Failed to resolve new type {}", ptd.typeName());
				throw new ComExceptionRemoteClassNotFound(ptd.typeName());
			}
		}
	
		return null;
	}
	
	@Override
	public Object processMessage(final Object messageObject)
	{
		final ComMessageNewType message = (ComMessageNewType)messageObject;
		return this.processMessage(message);
	}

	@Override
	public boolean continueReceiving()
	{
		return true;
	}
}
