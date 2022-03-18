
package one.microstream.examples.customlegacytypehandler;

/*-
 * #%L
 * microstream-examples-custom-legacy-type-handler
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

public class Location
{
	String directions;
	double latitude;
	double longitude;
	
	public Location(final String directions, final double latitude, final double longitude)
	{
		super();
		this.directions = directions;
		this.latitude   = latitude;
		this.longitude  = longitude;
	}
	
	@Override
	public String toString()
	{
		return "Latitude: " + this.latitude + "\nLogitude: " + this.longitude + "\ndirections: " + this.directions;
	}
}
