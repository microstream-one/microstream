
package one.microstream.entity.codegen;

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

import javax.lang.model.element.ExecutableElement;

import one.microstream.chars.VarString;
import one.microstream.entity.EntityException;


@SuppressWarnings("exports")
public class EntityExceptionInvalidEntityMethod extends EntityException
{
	private final ExecutableElement method;
	
	public EntityExceptionInvalidEntityMethod(final ExecutableElement method)
	{
		super();
		
		this.method = method;
	}
	
	public final ExecutableElement method()
	{
		return this.method;
	}
	
	@Override
	public String assembleDetailString()
	{
		return VarString.New()
			.add("Invalid entity method: ")
			.add(this.method.getEnclosingElement()).add('#').add(this.method)
			.add("; only methods with return type, no type parameters")
			.add(", no parameters and no checked exceptions are supported.")
			.toString();
	}
}
