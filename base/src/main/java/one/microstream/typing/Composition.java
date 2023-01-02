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

/**
 * Marker type to indicate that a certain implementation is a composition (is composed of unshared objects).
 * This is useful (or even necessary) for having a means of distinguishing generically handleable implementations
 * from implementations that require (or at least suggest) tailored generic treatment, e.g. for a persistence layer
 * to persist the unshared objects in an inlined fashion rather than storing an external reference.
 * <p>
 * This is done via an interface instead of an annotations because the design aspect to be represented is a typical
 * "is-a" relation and because annotation should actually not alter program behavior.
 *
 * @see ComponentType
 *
 * 
 */
public interface Composition
{
	// Marker interface
}
