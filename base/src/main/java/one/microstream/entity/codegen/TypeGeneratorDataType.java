
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

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;

import one.microstream.entity.EntityData;


class TypeGeneratorDataType extends TypeGenerator
{
	final static String SUFFIX = "Data";
	
	TypeGeneratorDataType(
		final EntityProcessor processor,
		final TypeElement entityTypeElement,
		final List<Member> members)
	{
		super(
			processor,
			entityTypeElement,
			members,
			true,
			SUFFIX);
	}
	
	@Override
	void generateCode()
	{
		final List<? extends TypeParameterElement> typeParameters         = this.entityTypeElement.getTypeParameters();
		final String                               typeParametersDeclCode =
			this.createTypeParameterDeclCode(typeParameters);
		final String                               typeParametersNameCode =
			this.createTypeParameterNameCode(typeParameters);
		
		this.add("public class ").add(this.typeName).add(typeParametersDeclCode)
			.add(" extends ").add(this.addImport(EntityData.class))
			.add(" implements ").add(this.entityName).add(typeParametersNameCode)
			.newline();
		this.add("{").newline();
		
		// fields
		this.members.forEach(
			m -> this.tab().add("private final ").add(m.paddedTypeName).blank().add(m.paddedName).add(";").newline());
		
		// constructor
		this.newline()
			.tab().add("protected ").add(this.typeName).add("(final ")
			.add(this.entityName).add(typeParametersNameCode).add(" entity");
		this.members.forEach(
			m -> this.add(",").newline().tab(2).add("final ").add(m.paddedTypeName).blank().add(m.paddedName));
		this.add(")").newline()
			.tab().add("{").newline()
			.tab(2).add("super(entity);").newline();
		this.members.forEach(
			m -> this.newline().tab(2).add("this.").add(m.paddedName).add(" = ").add(m.paddedName).add(";"));
		this.newline().tab().add("}");
		
		// getter methods
		this.members.forEach(
			m -> this.newline().newline()
				.tab().add("@Override").newline()
				.tab().add("public ").add(m.typeName).blank().add(m.methodName).add("()").add(m.throwsClause).newline()
				.tab().add("{").newline()
				.tab(2).add("return this.").add(m.name).add(";").newline()
				.tab().add("}"));
		
		if(this.processor.getGenerateAppendable())
		{
			this.newline().newline().tab().add("@Override").newline()
				.tab().add("public String toString()").newline()
				.tab().add("{").newline()
				.tab(2).add("return ").add(this.getGeneratedTypeName(TypeGeneratorAppendableType.SUFFIX))
				.add(".toString(this);").newline()
				.tab().add("}");
		}
		
		this.newline().add("}");
	}
}
