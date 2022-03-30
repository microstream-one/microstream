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

import one.microstream.reference.ObjectSwizzling;

public interface PersistenceStoreHandler<D> extends PersistenceFunction
{
	/**
	 * The "natural" way of handling an instance as defined by the implementation.
	 * 
	 * @param <T> the type of the instance
	 * @param instance the instance to store
	 * @return the assigned object id
	 */
	@Override
	public <T> long apply(T instance);
	
	/**
	 * A way to signal to the implementation that the passed instance is supposed to be handled eagerly,
	 * meaning it shall be handled even if the handling implementation does not deem it necessary.<br>
	 * This is needed, for example, to store composition pattern instances without breaking OOP encapsulation concepts.
	 * 
	 * @param <T> the type of the instance
	 * @param instance the instance to store
	 * @return the assigned object id
	 */
	public <T> long applyEager(T instance);
	
	public <T> long apply(T instance, PersistenceTypeHandler<D, T> localTypeHandler);
	
	public <T> long applyEager(T instance, PersistenceTypeHandler<D, T> localTypeHandler);
	
	public ObjectSwizzling getObjectRetriever();
	
}
