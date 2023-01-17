package one.microstream.reference;

/*-
 * #%L
 * MicroStream Base
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
 * Classes that implement that interface can utilize {@link one.microstream.reference.ObservedLazyReference.Default}
 * to gain control of the unloading of {@link Lazy} references.
 */
public interface LazyClearObserver
{
	/**
	 * Allow or deny clearing a lazy reference.
	 * 
	 * @return true if clearing the lazy reference is allowed, otherwise false.
	 */
	public boolean allowClear();
}
