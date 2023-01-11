
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


public class EntityExceptionIdentityMismatch extends EntityException
{
	private final Entity entity1;
	private final Entity entity2;
	
	public EntityExceptionIdentityMismatch(final Entity entity1, final Entity entity2)
	{
		super();
		
		this.entity1 = entity1;
		this.entity2 = entity2;
	}
	
	public final Entity entity1()
	{
		return this.entity1;
	}
	
	public final Entity entity2()
	{
		return this.entity2;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Entity identity mismatch: ")
			.add(systemString(this.entity1))
			.add(" != ")
			.add(systemString(this.entity2))
			.toString();
	}
}
