
package one.microstream.wrapping.codegen;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import one.microstream.chars.VarString;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.wrapping.Wrapper;

class WrapperTypeGenerator
{
	private final static String         JAVA_LANG_PACKAGE = "java.lang";
	
	final ProcessingEnvironment         processingEnv;
	final TypeElement                   wrappedTypeElement;
	final Collection<ExecutableElement> methods;
	final String                        wrappedName;
	final String                        typeName;
	final String                        packageName;
	
	private final Map<String, String>   imports           = new HashMap<>();
	private final VarString             source            = VarString.New();
	
	WrapperTypeGenerator(
		final ProcessingEnvironment         processingEnv     ,
		final TypeElement                   wrappedTypeElement,
		final Collection<ExecutableElement> methods
	)
	{
		super();
		
		this.processingEnv      = processingEnv;
		this.wrappedTypeElement = wrappedTypeElement;
		this.methods            = methods;
		
		this.wrappedName        = wrappedTypeElement.getSimpleName().toString();
		this.typeName           = "Wrapper".concat(this.wrappedName);
		this.packageName        =
			processingEnv.getElementUtils().getPackageOf(wrappedTypeElement).getQualifiedName().toString()
				.concat("._wrapper");
	}
	
	final void generateType()
	{
		this.processingEnv.getMessager().printMessage(Kind.NOTE,
			VarString.New("Generating ").add(this.packageName).add('.').add(this.typeName).toString());
		
		this.generateCode();
		
		this.writeFile();
	}
	
	void generateCode()
	{
		this.add("public interface ").add(this.typeName)
			.add(this.createTypeParameterDeclCode(this.wrappedTypeElement.getTypeParameters()))
			.add(" extends ").add(this.addImport(Wrapper.class)).add("<")
			.add(this.addImport(this.wrappedTypeElement.asType()))
			.add(">, ").add(this.wrappedName)
			.add(this.createTypeParameterNameCode(this.wrappedTypeElement.getTypeParameters()));
		this.newline().add("{");
		
		this.methods.forEach(method -> {
			
			final ExecutableType methodType = (ExecutableType)this.processingEnv.getTypeUtils().asMemberOf(
				(DeclaredType)this.wrappedTypeElement.asType(),
				method);
			
			if(this.hasGenericVarArgs(method, methodType))
			{
				this.newline().tab().add("@SuppressWarnings(\"unchecked\")");
			}
			this.newline().tab().add("@Override").newline().tab().add("public default ");
			
			final String typeParams = this.createTypeParameterDeclCode(method.getTypeParameters());
			if(typeParams.length() > 0)
			{
				this.add(typeParams).blank();
			}
			
			this.add(this.addImport(methodType.getReturnType()))
				.blank().add(method.getSimpleName().toString()).add("(");
			
			final List<? extends VariableElement> parameters = method.getParameters();
			for(int i = 0, c = parameters.size(); i < c; i++)
			{
				if(i > 0)
				{
					this.add(", ");
				}
				
				this.add("final ");
				
				final TypeMirror paramType = methodType.getParameterTypes().get(i);
				if(i == c - 1 && method.isVarArgs())
				{
					this.add(this.addImport(((ArrayType)paramType).getComponentType())).add("...");
				}
				else
				{
					this.add(this.addImport(paramType));
				}
				
				this.blank().add(parameters.get(i).getSimpleName().toString());
			}
			
			this.add(")");
			
			final List<? extends TypeMirror> thrownTypes = methodType.getThrownTypes();
			if(thrownTypes.size() > 0)
			{
				this.add(" throws ")
					.add(thrownTypes.stream()
						.map(this::addImport)
						.collect(Collectors.joining(", ")));
			}
			
			this.newline().tab().add("{").newline().tab(2);
			if(methodType.getReturnType().getKind() != TypeKind.VOID)
			{
				this.add("return ");
			}
			this.add("this.wrapped().").add(method.getSimpleName().toString()).add("(");
			this.add(parameters.stream().map(p -> p.getSimpleName().toString()).collect(Collectors.joining(", ")));
			this.add(");").newline();
			this.tab().add("}").newline();
		});
		
		this.add("}");
	}
	
	private boolean hasGenericVarArgs(final ExecutableElement method, final ExecutableType methodType)
	{
		return method.isVarArgs()
			&& this.getVarArgType(method, methodType).getKind() == TypeKind.TYPEVAR;
	}
	
	private TypeMirror getVarArgType(final ExecutableElement method, final ExecutableType methodType)
	{
		final List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
		final TypeMirror                 last           = parameterTypes.get(parameterTypes.size() - 1);
		return ((ArrayType)last).getComponentType();
	}
	
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
			(DeclaredType)this.processingEnv.getTypeUtils().erasure(declaredType);
		final TypeElement  typeErasureElement  = (TypeElement)declaredTypeErasure.asElement();
		
		final String       simpleName          = typeErasureElement.getSimpleName().toString();
		final String       qualifiedName       = typeErasureElement.getQualifiedName().toString();
		final String       packageName         =
			this.processingEnv.getElementUtils().getPackageOf(typeErasureElement).toString();
		
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
		final List<? extends TypeMirror> bounds = typeParam.getBounds().stream()
			.filter(bound -> !bound.toString().equals(Object.class.getName()))
			.collect(Collectors.toList());
		return bounds.isEmpty()
			? name
			: name + bounds.stream().map(this::addImport).collect(Collectors.joining(" & ", " extends ", ""));
	}
	
	WrapperTypeGenerator add(final String code)
	{
		this.source.add(code);
		return this;
	}
	
	WrapperTypeGenerator blank()
	{
		this.source.blank();
		return this;
	}
	
	WrapperTypeGenerator tab()
	{
		this.source.tab();
		return this;
	}
	
	WrapperTypeGenerator tab(final int amount)
	{
		this.source.tab(amount);
		return this;
	}
	
	WrapperTypeGenerator newline()
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
			final JavaFileObject file = this.processingEnv.getFiler().createSourceFile(
				this.packageName + "." + this.typeName,
				this.wrappedTypeElement);
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
