
package one.microstream.integrations.cdi.types.extension;

/*-
 * #%L
 * microstream-integrations-cdi
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

import java.time.LocalDate;
import java.util.Map;


public class Contact
{
	private final LocalDate           date    ;
	private final String              owner   ;
	private final Map<String, String> contacts;
	
	public Contact(final LocalDate date, final String owner, final Map<String, String> contacts)
	{
		this.date     = date    ;
		this.owner    = owner   ;
		this.contacts = contacts;
	}
	
	public LocalDate getDate()
	{
		return this.date;
	}
	
	public String getOwner()
	{
		return this.owner;
	}
	
	public Map<String, String> getContacts()
	{
		return this.contacts;
	}
}
