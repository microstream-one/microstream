package one.microstream.examples.extensionwrapper;

/*-
 * #%L
 * microstream-examples-extension-wrapper
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;

/**
 * Wrapper for {@link PersistenceTarget}, used as base for extensions
 *
 */
public class PersistenceTargetWrapper implements PersistenceTarget<Binary>
{
	private final PersistenceTarget<Binary> delegate;

	public PersistenceTargetWrapper(final PersistenceTarget<Binary> delegate)
	{
		super();
		this.delegate = delegate;
	}

	@Override
	public boolean isWritable()
	{
		return this.delegate.isWritable();
	}

	@Override
	public void write(final Binary data) throws PersistenceExceptionTransfer
	{
		this.delegate.write(data);
	}
	
	
}
