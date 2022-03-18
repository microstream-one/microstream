package one.microstream.typing;

/*-
 * #%L
 * microstream-base
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

import one.microstream.hashing.HashImmutable;


/**
 * A type whose instances represents (effectively) immutable values that should only be primarily handled as values
 * instead of objects (e.g. for determining equality and comparison). String, primitive wrappers, etc. should have been
 * marked with an interface like that. Sadly, they aren't. Nevertheless, here is a proper marker interface
 * to mark self defined types as being value types.
 * <p>
 * Value types are the only types where inherently implemented equals() and hashCode() are properly applicable.
 * As Java is sadly missing a SELF typing, the untyped equals(Object obj) can't be defined more specific
 * (like for example public boolean equals(SELF obj) or such).
 *
 * <p>
 * Also see:
 * @see HashImmutable
 * @see Immutable
 * @see Stateless
 *
 * 
 *
 */
public interface ValueType extends HashImmutable
{
	// marker interface
}
