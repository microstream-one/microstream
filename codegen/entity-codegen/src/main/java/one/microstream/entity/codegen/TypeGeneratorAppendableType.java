
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

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;


class TypeGeneratorAppendableType extends TypeGenerator
{
	final static String SUFFIX = "Appendable";
	
	TypeGeneratorAppendableType(
		final EntityProcessor processor,
		final TypeElement entityTypeElement,
		final List<Member> members)
	{
		super(
			processor,
			entityTypeElement,
			members,
			false,
			SUFFIX);
	}
	
	@Override
	void generateCode()
	{
		final List<? extends TypeParameterElement> typeParameters     =
			this.entityTypeElement.getTypeParameters();
		final String                               typeParametersCode = typeParameters.isEmpty()
			? ""
			: typeParameters.stream().map(tp -> "?").collect(Collectors.joining(", ", "<", ">"));
		final String                               varName            = XChars.decapitalize(this.entityName);
		
		this.add("public interface ").add(this.typeName)
			.add(" extends ").add(this.addImport(VarString.class)).add(".Appendable")
			.newline().add("{");
		
		// toString helper
		this.newline().tab().add("public static String toString(final ")
			.add(this.entityName).add(typeParametersCode).blank().add(varName).add(")").newline()
			.tab().add("{").newline()
			.tab(2).add("return New(").add(varName).add(").appendTo(VarString.New()).toString();").newline()
			.tab().add("}").newline();
		
		// pseudo constructor
		this.newline().tab().add("public static ").add(this.typeName)
			.add(" New(final ").add(this.entityName).add(typeParametersCode).blank().add(varName).add(")").newline()
			.tab().add("{").newline()
			.tab(2).add("return new Default(").add(varName).add(");").newline()
			.tab().add("}").newline();
		
		// default implementation class
		
		this.newline().tab().add("public static class Default implements ")
			.add(this.typeName).newline()
			.tab().add("{").newline();
		
		// field
		this.tab(2).add("private final ").add(this.entityName).add(typeParametersCode).blank().add(varName).add(";");
		
		// constructor
		this.newline().newline().tab(2).add("Default(final ")
			.add(this.entityName).add(typeParametersCode).blank().add(varName).add(")").newline()
			.tab(2).add("{").newline()
			.tab(3).add("super();").newline().newline()
			.tab(3).add("this.").add(varName).add(" = ").add(varName).add(";").newline()
			.tab(2).add("}").newline().newline();
		
		// override appendTo(VarString)
		this.tab(2).add("@Override").newline()
			.tab(2).add("public VarString appendTo(final VarString vs)").newline()
			.tab(2).add("{").newline()
			.tab(3).add("return vs.add(this.").add(varName).add(".getClass().getSimpleName())").newline();
		boolean first = true;
		for(final Member m : this.members)
		{
			this.tab(4).add(".add(\"");
			if(first)
			{
				this.add(" [");
				first = false;
			}
			else
			{
				this.add(", ");
			}
			this.add(m.name).add(" = \")").newline()
				.tab(4).add(".add(this.").add(varName).add(".").add(m.methodName).add("())").newline();
		}
		this.tab(4).add(".add(']');").newline();
		this.tab(2).add("}").newline();
		this.tab().add("}").newline();
		this.add("}");
	}
}
