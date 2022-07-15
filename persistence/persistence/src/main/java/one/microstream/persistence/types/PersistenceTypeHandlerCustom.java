package one.microstream.persistence.types;

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

/**
 * This type extends the {@link PersistenceTypeHandler} type only by the following reflection contract:<p>
 * A class implementing this type can use arbitrary logic to translate instances of the handled type to
 * their persistent form and back.
 *
 * 
 * @param <D> the data type
 * @param <T> the handled type
 * 
 * @see PersistenceTypeHandlerGeneric
 */
public interface PersistenceTypeHandlerCustom<D, T> extends PersistenceTypeHandler<D, T>
{
	// typing interface only (so far)
}
