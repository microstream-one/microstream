package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

public interface StorageChannelResetablePart extends StorageHashChannelPart
{
	/**
	 * Closes all resources (files, locks, etc.).
	 * Clears all variable length items (cache, registry, etc.).
	 * Resets internal state to initial values.
	 * For itself and all its parts (entity cache, file manager, etc.).
	 * Basically a "back to just being born" action.
	 */
	public void reset();
}
