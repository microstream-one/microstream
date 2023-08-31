
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
import javax.lang.model.type.TypeMirror;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;

class Member
{
	final ExecutableElement element;
	final String            methodName;
	final String            name;
	final String            setterName;
	final TypeMirror        type;
	
	String                  typeName;
	String                  paddedTypeName;
	String                  paddedMethodName;
	String                  paddedName;
	String                  throwsClause;
	
	Member(final ExecutableElement element, final TypeMirror type)
	{
		super();
		
		this.element    = element;
		this.methodName = element.getSimpleName().toString();
		this.name       = memberName(this.methodName);
		this.setterName = setterName(this.name);
		this.type       = type;
	}
	
	private static String memberName(final String methodName)
	{
		int offset = -1;
		if(methodName.startsWith("get"))
		{
			offset = 3;
		}
		else if(methodName.startsWith("is"))
		{
			offset = 2;
		}
		
		return offset <= 0 || methodName.length() <= offset
			? methodName
			: XChars.decapitalize(methodName.substring(offset));
	}
	
	private static String setterName(final String name)
	{
		return VarString.New("set")
			.add(Character.toUpperCase(name.charAt(0)))
			.add(name.substring(1))
			.toString();
	}
}
