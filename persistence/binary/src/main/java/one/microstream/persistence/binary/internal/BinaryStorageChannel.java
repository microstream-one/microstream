package one.microstream.persistence.binary.internal;

/*-
 * #%L
 * microstream-persistence-binary
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

import one.microstream.collections.types.XGettingCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceChannel;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceTarget;

public final class BinaryStorageChannel implements PersistenceChannel<Binary>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceSource<Binary> source;
	private final PersistenceTarget<Binary> target;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryStorageChannel(
		final PersistenceSource<Binary> source,
		final PersistenceTarget<Binary> target
	)
	{
		super();
		this.source = notNull(source);
		this.target = notNull(target);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void write(final Binary data) throws PersistenceExceptionTransfer
	{
		this.target.write(data);
	}

	@Override
	public final XGettingCollection<? extends Binary> read() throws PersistenceExceptionTransfer
	{
		return this.source.read();
	}

	@Override
	public final XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
		throws PersistenceExceptionTransfer
	{
		return this.source.readByObjectIds(oids);
	}
	
	@Override
	public final void validateIsWritable()
	{
		this.target.validateIsWritable();
	}
	
	@Override
	public final boolean isWritable()
	{
		return this.target.isWritable();
	}
	
	@Override
	public void validateIsStoringEnabled()
	{
		this.target.validateIsStoringEnabled();
	}
	
	@Override
	public boolean isStoringEnabled()
	{
		return this.target.isStoringEnabled();
	}

}
