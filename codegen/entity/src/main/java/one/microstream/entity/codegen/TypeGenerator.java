
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

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import one.microstream.chars.VarString;
import one.microstream.exceptions.IORuntimeException;


abstract class TypeGenerator
{
	private final static String       JAVA_LANG_PACKAGE = "java.lang";
	private final static String       GENERATED_PREFIX  = "_";
	
	final EntityProcessor             processor;
	final TypeElement                 entityTypeElement;
	final List<Member>                members;
	final String                      entityName;
	final String                      packageName;
	final String                      typeName;
	
	private final Map<String, String> imports           = new HashMap<>();
	private final VarString           source            = VarString.New();
	
	TypeGenerator(
		final EntityProcessor processor,
		final TypeElement entityTypeElement,
		final List<Member> members,
		final boolean memberTypesUsed,
		final String typeNameSuffix)
	{
		super();
		
		this.processor         = processor;
		this.entityTypeElement = entityTypeElement;
		
		this.entityName        = entityTypeElement.getSimpleName().toString();
		this.packageName       = VarString.New()
			.add(processor.getEnvironment().getElementUtils()
				.getPackageOf(entityTypeElement).getQualifiedName().toString())
			.add('.').add(GENERATED_PREFIX).add(this.entityName)
			.toString();
		
		this.typeName          = this.getGeneratedTypeName(typeNameSuffix);
		this.members           = this.prepareMembers(members, memberTypesUsed);
		
		// entity type is always used
		this.addImport(entityTypeElement.asType());
	}
	
	String getGeneratedTypeName(final String suffix)
	{
		return this.entityName.concat(suffix);
	}
	
	private List<Member> prepareMembers(final List<Member> members, final boolean memberTypesUsed)
	{
		/*
		 * Flag handed over by subtypes, to avoid polluting the imports section with unused types, if necessary.
		 */
		if(memberTypesUsed)
		{
			members.forEach(m -> m.typeName = this.addImport(m.type));
			final int maxTypeNameLength = members.stream().mapToInt(m -> m.typeName.length()).max().getAsInt();
			members.forEach(m -> {
				m.paddedTypeName = this.rpad(m.typeName, maxTypeNameLength);
				m.throwsClause   = this.createThrowsClause(m);
			});
		}
		
		final int maxMethodNameLength = members.stream().mapToInt(m -> m.methodName.length()).max().getAsInt();
		final int maxNameLength       = members.stream().mapToInt(m -> m.name.length()).max().getAsInt();
		members.forEach(m -> {
			m.paddedMethodName = this.rpad(m.methodName, maxMethodNameLength);
			m.paddedName       = this.rpad(m.name, maxNameLength);
		});
		
		return members;
	}
	
	private String rpad(final String string, final int maxLength)
	{
		final int length = string.length();
		return length >= maxLength
			? string
			: VarString.New(string).blank(maxLength - length).toString();
	}
	
	private String createThrowsClause(final Member member)
	{
		final List<? extends TypeMirror> thrownTypes = member.element.getThrownTypes();
		return thrownTypes.isEmpty()
			? ""
			: thrownTypes.stream()
				.map(this::addImport)
				.collect(Collectors.joining(", ", " throws ", ""));
	}
	
	final void generateType()
	{
		this.processor.getEnvironment().getMessager().printMessage(Kind.NOTE,
			VarString.New("Generating ").add(this.packageName).add('.').add(this.typeName).toString());
		
		this.generateCode();
		this.writeFile();
	}
	
	abstract void generateCode();
	
	String addImport(final TypeMirror type)
	{
		if(type.getKind() == TypeKind.ARRAY)
		{
			return this.addImport(((ArrayType)type).getComponentType()).concat("[]");
		}
		
		if(type.getKind() != TypeKind.DECLARED)
		{
			return type.toString();
		}
		
		final DeclaredType declaredType        = (DeclaredType)type;
		
		final DeclaredType declaredTypeErasure =
			(DeclaredType)this.processor.getEnvironment().getTypeUtils().erasure(declaredType);
		final TypeElement  typeErasureElement  = (TypeElement)declaredTypeErasure.asElement();
		
		final String       simpleName          = typeErasureElement.getSimpleName().toString();
		final String       qualifiedName       = typeErasureElement.getQualifiedName().toString();
		final String       packageName         =
			this.processor.getEnvironment().getElementUtils().getPackageOf(typeErasureElement).toString();
		
		final VarString    vs                  = VarString.New();
		
		if(JAVA_LANG_PACKAGE.equals(packageName) || this.packageName.equals(packageName))
		{
			vs.add(simpleName);
		}
		else
		{
			final String mappedQualifiedName = this.imports.computeIfAbsent(simpleName, key -> qualifiedName);
			if(mappedQualifiedName.equals(qualifiedName))
			{
				vs.add(simpleName);
			}
			else
			{
				vs.add(qualifiedName);
			}
		}
		
		final List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
		if(typeArguments.size() > 0)
		{
			vs.add(typeArguments.stream()
				.map(this::addImport)
				.collect(Collectors.joining(", ", "<", ">")));
		}
		
		return vs.toString();
	}
	
	String addImport(final Class<?> type)
	{
		final String simpleName = type.getSimpleName();
		if(JAVA_LANG_PACKAGE.equals(type.getPackage().getName()))
		{
			return simpleName;
		}
		
		final String qualifiedName       = type.getCanonicalName();
		final String mappedQualifiedName = this.imports.computeIfAbsent(simpleName, key -> qualifiedName);
		return mappedQualifiedName.equals(qualifiedName)
			? simpleName
			: qualifiedName;
	}
	
	String createTypeParameterNameCode(final List<? extends TypeParameterElement> typeParameters)
	{
		return typeParameters.isEmpty()
			? ""
			: typeParameters.stream()
				.map(tp -> tp.getSimpleName().toString())
				.collect(Collectors.joining(", ", "<", ">"));
	}
	
	String createTypeParameterDeclCode(final List<? extends TypeParameterElement> typeParameters)
	{
		return typeParameters.isEmpty()
			? ""
			: typeParameters.stream()
				.map(tp -> this.createTypeParameterDeclCode(tp))
				.collect(Collectors.joining(", ", "<", ">"));
	}
	
	private String createTypeParameterDeclCode(final TypeParameterElement typeParam)
	{
		final String                     name   = typeParam.getSimpleName().toString();
		final List<? extends TypeMirror> bounds = typeParam.getBounds();
		return bounds.isEmpty() || (bounds.size() == 1 && this.isJavaLangObject(bounds.get(0)))
			? name
			: name + bounds.stream().map(this::addImport).collect(Collectors.joining(" & ", " extends ", ""));
	}
	
	boolean isJavaLangObject(final TypeMirror type)
	{
		return Object.class.getName().equals(this.getFullQualifiedName(type));
	}
	
	String getFullQualifiedName(final TypeMirror type)
	{
		final ProcessingEnvironment environment = this.processor.getEnvironment();
		final TypeElement           element     = (TypeElement)environment.getTypeUtils().asElement(type);
		return element.getQualifiedName().toString();
	}
	
	TypeGenerator add(final String code)
	{
		this.source.add(code);
		return this;
	}
	
	TypeGenerator blank()
	{
		this.source.blank();
		return this;
	}
	
	TypeGenerator tab()
	{
		this.source.tab();
		return this;
	}
	
	TypeGenerator tab(final int amount)
	{
		this.source.tab(amount);
		return this;
	}
	
	TypeGenerator newline()
	{
		this.source.add(System.lineSeparator());
		return this;
	}
	
	String getCode()
	{
		final String    lineSeparator = System.lineSeparator();
		
		final VarString vs            = VarString.New();
		vs.add("package ").add(this.packageName).add(";").add(lineSeparator);
		vs.add(lineSeparator);
		if(this.imports.size() > 0)
		{
			this.imports.values().forEach(path -> vs.add("import ").add(path).add(";").add(lineSeparator));
			vs.add(lineSeparator);
			vs.add(lineSeparator);
		}
		vs.add(this.source);
		return vs.toString();
	}
	
	private void writeFile()
	{
		try
		{
			final JavaFileObject file = this.processor.getEnvironment().getFiler().createSourceFile(
				this.packageName + "." + this.typeName,
				this.entityTypeElement);
			try(Writer writer = file.openWriter())
			{
				writer.write(this.getCode());
			}
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
}
