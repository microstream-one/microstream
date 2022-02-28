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

import java.nio.ByteBuffer;

import org.slf4j.Logger;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.communication.types.ComConnection;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.persistence.types.PersistenceTypeDictionaryAssembler;
import one.microstream.persistence.types.PersistenceTypeDictionaryView;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.util.logging.Logging;

/**
 * This class handles the matching of types that have been modified on either the client or the host side
 * Including the necessary data transfer during the initialization of the ComChannels.
 *
 */
public class ComTypeMappingResolver
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	protected static final int LENGTH_CHAR_COUNT = 8;
		
	private final static Logger logger = Logging.getLogger(ComHandlerSendMessageNewType.class);
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	protected final PersistenceTypeDictionaryAssembler    typeDictionaryAssembler;
	protected final ComConnection                         connection;
	protected final PersistenceTypeDictionaryView         hostTypeDictionary;
	protected final PersistenceTypeHandlerManager<Binary> typeHandlerManager;
	protected final ComTypeDefinitionBuilder              typeDefinitionBuilder;
		
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Constructs a ComTypeMappingResolver instance
	 * 
	 * @param typeDictionaryAssembler PersistenceTypeDictionaryAssembler
	 * @param connection			  ComConnection
	 * @param hostTypeDictionary      PersistenceTypeDictionaryView
	 * @param typeHandlerManager      PersistenceTypeHandlerManager
	 * @param typeDefinitionBuilder   ComTypeDefinitionBuilder
	 */
	public ComTypeMappingResolver(
		final PersistenceTypeDictionaryAssembler    typeDictionaryAssembler,
		final ComConnection                         connection,
		final PersistenceTypeDictionaryView         hostTypeDictionary,
		final PersistenceTypeHandlerManager<Binary> typeHandlerManager,
		final ComTypeDefinitionBuilder              typeDefinitionBuilder
	)
	{
		super();
		this.typeDictionaryAssembler = typeDictionaryAssembler;
		this.connection              = connection;
		this.hostTypeDictionary      = hostTypeDictionary;
		this.typeHandlerManager      = typeHandlerManager;
		this.typeDefinitionBuilder   = typeDefinitionBuilder;
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	/**
	 * Handle the client's side of the communication type mapping during connection initialization phase.
	 * This is collection all type definition that belong to the clients classes that needs to be mapped by the host
	 * and transferring those to the host.
	 */
	public void resolveClient()
	{
		logger.debug("resolving client type mappings ");
		
		this.sendNewTypeDefintionsToHost(
			this.assembleTypeDefinitions(
				this.findHostTypeDefinitions()));
	}
	
	/**
	 * Handle the host's side of the communication type mapping during connection initialization phase.
	 * This is receiving the client's type definitions and creating the required legacy type handlers.
	 */
	public void resolveHost()
	{
		logger.debug("resolving host type mappings ");
		
		this.applyHostTypeMapping(
			this.parseClientTypeDefinitions(
				this.receiveUpdatedDefintionsfromClient()));
	}
	
	private void sendNewTypeDefintionsToHost(final byte[] assembledTypeDefinitions)
	{
		logger.trace("transfering new type defintions to host");
		
		final ByteBuffer dbb = XMemory.allocateDirectNative(assembledTypeDefinitions.length);
		final long dbbAddress = XMemory.getDirectByteBufferAddress(dbb);
		XMemory.copyArrayToAddress(assembledTypeDefinitions, dbbAddress);
		
		this.connection.writeCompletely(dbb);
	}

	private byte[] assembleTypeDefinitions(final BulkList<PersistenceTypeDescription> newDefinitions)
	{
		final VarString vs = VarString.New(10_000);
		
		vs
		.reset()
		.repeat(LENGTH_CHAR_COUNT, '0')
		.add(String.format("%08d", newDefinitions.intSize()));
		
		newDefinitions.forEach(definition -> {
			vs.add(this.assembleTypeDefintion(definition));
		});
		
		final char[] lengthString = XChars.readChars(XChars.String(vs.length()));
		vs.setChars(LENGTH_CHAR_COUNT - lengthString.length, lengthString);
		
		return vs.encode();
	}

	private VarString assembleTypeDefintion(final PersistenceTypeDescription definition)
	{
		final VarString vc = VarString.New();
		this.typeDictionaryAssembler.assembleTypeDescription(vc, definition);
		return vc;
	}

	private BulkList<PersistenceTypeDescription> findHostTypeDefinitions()
	{
		final BulkList<PersistenceTypeDescription> newTypeDescriptions = BulkList.New();
		
		this.typeHandlerManager.iterateLegacyTypeHandlers(legacyTypeHandler -> {
			final PersistenceTypeHandler<Binary, ?> currentHandler = this.typeHandlerManager.lookupTypeHandler(legacyTypeHandler.type());
			if(this.hostTypeDictionary.lookupTypeById(currentHandler.typeId()) == null)
			{
				newTypeDescriptions.add(currentHandler);
				logger.trace("new type found for id {}", currentHandler.typeId());
			}
		});
		
		logger.debug("{} new types found", newTypeDescriptions.size());
		return newTypeDescriptions;
	}
	
	private XGettingSequence<PersistenceTypeDefinition> parseClientTypeDefinitions(final ByteBuffer buffer)
	{
		if(buffer != null)
		{
			buffer.position(1);
			final char[] typeDefinitionsChars = XChars.standardCharset().decode(buffer).array();
		
			final String typeDefintions = XChars.String(typeDefinitionsChars);
			final XGettingSequence<PersistenceTypeDefinition> newTypeDescriptions = this.typeDefinitionBuilder.buildTypeDefinitions(typeDefintions);
			
			logger.debug("received {} types from client", newTypeDescriptions.size());
			return newTypeDescriptions;
		}
		
		logger.debug("received 0 types from client");
		return BulkList.New();
	}

	private ByteBuffer receiveUpdatedDefintionsfromClient()
	{
		logger.trace("receiving new type defintions from client");
		
		final ByteBuffer lengthBuffer = XMemory.allocateDirectNative(LENGTH_CHAR_COUNT);
		this.connection.read(lengthBuffer, LENGTH_CHAR_COUNT);
		
		lengthBuffer.position(0);
		final String lengthDigits = XChars.standardCharset().decode(lengthBuffer).toString();
		final int    length       = Integer.parseInt(lengthDigits);
		
		final ByteBuffer countBuffer = XMemory.allocateDirectNative(LENGTH_CHAR_COUNT);
		this.connection.read(countBuffer, LENGTH_CHAR_COUNT);
		countBuffer.position(0);
		final String countDigits = XChars.standardCharset().decode(countBuffer).toString();
		final int    count       = Integer.parseInt(countDigits);
		
		if(count > 0 )
		{
			final ByteBuffer typeDefinitionsBuffer = XMemory.allocateDirectNative(length - LENGTH_CHAR_COUNT - LENGTH_CHAR_COUNT);
			this.connection.read(typeDefinitionsBuffer, length - LENGTH_CHAR_COUNT - LENGTH_CHAR_COUNT);
			return typeDefinitionsBuffer;
		}
		
		return null;
	}
	
	private void applyHostTypeMapping(final XGettingSequence<PersistenceTypeDefinition> typeDefinitions)
	{
		if(typeDefinitions != null)
		{
			typeDefinitions.forEach( typeDefinition -> {
				final PersistenceTypeHandler<Binary, ?> currentHandler = this.typeHandlerManager.lookupTypeHandler(typeDefinition.type());
				this.typeHandlerManager.ensureLegacyTypeHandler(typeDefinition, currentHandler);
				this.typeHandlerManager.updateCurrentHighestTypeId(typeDefinition.typeId());
				
				logger.trace("type mapping applied for typeId {}", typeDefinition.typeId());
			});
		}
	}
}
