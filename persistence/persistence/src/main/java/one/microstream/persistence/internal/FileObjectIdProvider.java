package one.microstream.persistence.internal;

/*-
 * #%L
 * microstream-persistence
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

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import one.microstream.afs.types.AFile;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceObjectIdProvider;


public final class FileObjectIdProvider extends AbstractIdProviderByFile implements PersistenceObjectIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static FileObjectIdProvider New(final AFile file, final long increase)
	{
		return new FileObjectIdProvider(
			 notNull(file)                ,
			positive(increase)            ,
			Persistence.defaultStartObjectId()
		);
	}

	public static FileObjectIdProvider New(final AFile file, final long increase, final long startId)
	{
		return new FileObjectIdProvider(
			 notNull(file)                   ,
			positive(increase)               ,
			Persistence.validateObjectId(startId)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	FileObjectIdProvider(final AFile file, final long increase, final long startId)
	{
		super(file, increase, startId);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final long provideNextObjectId()
	{
		return this.next();
	}

	@Override
	public final long currentObjectId()
	{
		return this.current();
	}

	@Override
	public final FileObjectIdProvider initializeObjectId()
	{
		this.internalInitialize();
		return this;
	}

	@Override
	public FileObjectIdProvider updateCurrentObjectId(final long currentObjectId)
	{
		this.internalUpdateId(currentObjectId);
		return this;
	}

}
