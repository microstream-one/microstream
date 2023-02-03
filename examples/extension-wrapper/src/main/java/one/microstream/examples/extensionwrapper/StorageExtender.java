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

import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceTarget;

/**
 * Dispatcher logic which is used to extend certain parts
 */
public class StorageExtender implements InstanceDispatcherLogic
{
	@SuppressWarnings("unchecked")
	@Override
	public <T> T apply(final T subject)
	{
		if(subject instanceof PersistenceTarget)
		{
			return (T)new PersistenceTargetExtension((PersistenceTarget<Binary>)subject);
		}
		
		if(subject instanceof PersistenceStorer.Creator)
		{
			return (T)new PersistenceStorerExtension.Creator((PersistenceStorer.Creator<Binary>)subject);
		}
		
		return subject;
	}
}
