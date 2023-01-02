
package one.microstream.entity;

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

import static one.microstream.chars.XChars.systemString;

import one.microstream.chars.VarString;


public class EntityExceptionMissingDataForVersion extends EntityException
{
	private final Entity entity;
	private final Object versionKey;
	
	public EntityExceptionMissingDataForVersion(final Entity entity, final Object versionKey)
	{
		super();
		
		this.entity     = entity;
		this.versionKey = versionKey;
	}
	
	public final Entity entity()
	{
		return this.entity;
	}
	
	public final Object versionKey()
	{
		return this.versionKey;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Missing data for version '")
			.add(this.versionKey)
			.add("' in ")
			.add(systemString(this.entity))
			.toString();
	}
}
