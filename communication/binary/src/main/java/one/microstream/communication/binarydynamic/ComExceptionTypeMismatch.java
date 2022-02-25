package one.microstream.communication.binarydynamic;

/*-
 * #%L
 * MicroStream Communication Binary
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

import one.microstream.com.ComException;

public class ComExceptionTypeMismatch extends ComException
{
	private final long   typeId;
	private final String typeName;
	
	public ComExceptionTypeMismatch(final long typeId, final String typeName)
	{
		super(String.format("local type %s does not match to remote type with type id %d!",
			typeName,
			typeId
		));
		
		this.typeId = typeId;
		this.typeName = typeName;
	}

	protected long getTypeId()
	{
		return this.typeId;
	}

	protected String getType()
	{
		return this.typeName;
	}

}
