package one.microstream.communication.types;

/*-
 * #%L
 * microstream-communication
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

import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceTypeDictionaryView;
import one.microstream.persistence.types.PersistenceTypeDictionaryViewProvider;

public interface ComProtocolData extends PersistenceTypeDictionaryViewProvider
{
	public String name();
	
	public String version();
	
	public ByteOrder byteOrder();
	
	public PersistenceTypeDictionaryView typeDictionary();
	
	public PersistenceIdStrategy idStrategy();
	
	@Override
	public default PersistenceTypeDictionaryView provideTypeDictionary()
	{
		return this.typeDictionary();
	}

	public int inactivityTimeout();
	
}
