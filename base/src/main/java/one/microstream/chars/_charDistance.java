package one.microstream.chars;

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
 * Function type that calculates the distance or difference or cost between two given characters.
 * <p>
 * This is useful mostly for string procedure algorithms (e.g. Levenshtein distance).
 */
@FunctionalInterface
public interface _charDistance
{
	/**
	 * Calculates the distance of the two given characters.
	 * <p>
	 * The meaning of the returned value depends on the function implementation and/or the context it is used in.<br>
	 * Typical string similarity values range from 0.0 to 1.0 .
	 *
	 * @param a the first character
	 * @param b the second character
	 * @return the distance/difference/cost of the two given characters.
	 */
	public float distance(char a, char b);

}
