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

import java.nio.ByteOrder;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.communication.binary.types.ComPersistenceAdaptorBinary;
import one.microstream.communication.types.ComConnection;
import one.microstream.communication.types.ComPersistenceAdaptor;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.util.BufferSizeProvider;

public final class ComPersistenceAdaptorBinaryDynamicCreator extends ComPersistenceAdaptorBinary.Creator.Abstract<ComConnection>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected ComPersistenceAdaptorBinaryDynamicCreator(
		final BinaryPersistenceFoundation<?> foundation        ,
		final BufferSizeProvider             bufferSizeProvider
	)
	{
		super(foundation, bufferSizeProvider);
	}
		
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ComPersistenceAdaptor<ComConnection> createPersistenceAdaptor(
		final PersistenceIdStrategy  hostIdStrategyInitialization,
		final XGettingEnum<Class<?>> entityTypes                 ,
		final ByteOrder              hostByteOrder               ,
		final PersistenceIdStrategy  hostIdStrategy
	)
	{
		return ComPersistenceAdaptorBinaryDynamic.New(
			this.foundation()           ,
			this.bufferSizeProvider()   ,
			hostIdStrategyInitialization,
			entityTypes                 ,
			hostByteOrder               ,
			hostIdStrategy
		);
	}
}
