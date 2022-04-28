
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import one.microstream.chars.XChars;
import one.microstream.entity.Entity;


@SuppressWarnings("exports")
public class EntityProcessor extends AbstractProcessor
{
	private final static String     OPTION_HASHEQUALATOR = "microstream.entity.hashequalator";
	private final static String     OPTION_APPENDABLE    = "microstream.entity.appendable";
	
	private boolean                 generateHashEqualator;
	private boolean                 generateAppendable;
	
	private List<ExecutableElement> javaLangObjectMethods;
	private TypeMirror              runtimeExceptionType;
	private boolean                 processed            = false;
	
	public EntityProcessor()
	{
		super();
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion()
	{
		return SourceVersion.latestSupported();
	}
	
	@Override
	public Set<String> getSupportedAnnotationTypes()
	{
		/*
		 * 'Hack' to process all elements, not only annotated classes, since we don't use annotations.
		 */
		return Collections.singleton("*");
	}
	
	@Override
	public Set<String> getSupportedOptions()
	{
		final Set<String> set = new HashSet<>();
		set.add(OPTION_HASHEQUALATOR);
		set.add(OPTION_APPENDABLE);
		return set;
	}
	
	private boolean getBooleanOption(
		final String name,
		final boolean defaultValue)
	{
		String option;
		return XChars.isEmpty(option = this.processingEnv.getOptions().get(name))
			? defaultValue
			: Boolean.parseBoolean(option);
	}
	
	@Override
	public synchronized void init(final ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		
		this.javaLangObjectMethods = processingEnv.getElementUtils()
			.getTypeElement(Object.class.getName())
			.getEnclosedElements().stream()
			.filter(e -> e.getKind() == ElementKind.METHOD)
			.map(ExecutableElement.class::cast)
			.filter(method -> !method.getModifiers().contains(Modifier.STATIC))
			.collect(Collectors.toList());
		
		this.runtimeExceptionType  = processingEnv.getElementUtils()
			.getTypeElement(RuntimeException.class.getName())
			.asType();
		
		this.generateHashEqualator = this.getBooleanOption(OPTION_HASHEQUALATOR, true);
		this.generateAppendable    = this.getBooleanOption(OPTION_APPENDABLE, true);
	}
	
	boolean isGenerateHashEqualator()
	{
		return this.generateHashEqualator;
	}
	
	boolean getGenerateAppendable()
	{
		return this.generateAppendable;
	}
	
	ProcessingEnvironment getEnvironment()
	{
		return this.processingEnv;
	}
	
	@Override
	public boolean process(
		final Set<? extends TypeElement> annotations,
		final RoundEnvironment roundEnv)
	{
		if(roundEnv.processingOver() || this.processed)
		{
			return false;
		}
		
		roundEnv.getRootElements().stream()
			.filter(e -> e.getKind() == ElementKind.INTERFACE)
			.map(TypeElement.class::cast)
			.filter(this::isEntity)
			.forEach(this::generateTypes);
		
		this.processed = true;
		
		return false;
	}
	
	private boolean isEntity(final TypeElement typeElem)
	{
		return typeElem.getInterfaces().stream()
			.anyMatch(this::isEntity);
	}
	
	private boolean isEntity(final TypeMirror type)
	{
		if(type.getKind() == TypeKind.DECLARED)
		{
			final DeclaredType declaredType = (DeclaredType)type;
			final TypeElement  element      = (TypeElement)declaredType.asElement();
			if(element.getQualifiedName().contentEquals(Entity.class.getName()))
			{
				return true;
			}
			
			return this.isEntity(element);
		}
		
		return false;
	}
	
	private void generateTypes(final TypeElement entityTypeElement)
	{
		final Set<ExecutableElement> potentialMemberMethods = new LinkedHashSet<>();
		this.collectPotentialMemberMethods(entityTypeElement, potentialMemberMethods);
		potentialMemberMethods.forEach(this::validateMemberMethod);
		
		final DeclaredType        entityType     = (DeclaredType)entityTypeElement.asType();
		final List<Member>        members        = potentialMemberMethods.stream()
			.map(element -> new Member(element, this.getTypeInEntity(entityType, element)))
			.collect(Collectors.toList());
		
		final List<TypeGenerator> typeGenerators = new ArrayList<>(5);
		typeGenerators.add(new TypeGeneratorDataType(this, entityTypeElement, members));
		typeGenerators.add(new TypeGeneratorEntityIdentityType(this, entityTypeElement, members));
		typeGenerators.add(new TypeGeneratorCreatorType(this, entityTypeElement, members));
		typeGenerators.add(new TypeGeneratorUpdaterType(this, entityTypeElement, members));
		if(this.generateHashEqualator)
		{
			typeGenerators.add(new TypeGeneratorHashEqualatorType(this, entityTypeElement, members));
		}
		if(this.generateAppendable)
		{
			typeGenerators.add(new TypeGeneratorAppendableType(this, entityTypeElement, members));
		}
		typeGenerators.forEach(TypeGenerator::generateType);
	}
	
	private void collectPotentialMemberMethods(
		final TypeElement typeElement,
		final Set<ExecutableElement> members)
	{
		typeElement.getEnclosedElements().stream()
			.filter(e -> e.getKind() == ElementKind.METHOD)
			.map(ExecutableElement.class::cast)
			.filter(method -> this.isPotentialMemberMethod(method, members))
			.forEach(members::add);
		
		typeElement.getInterfaces().stream()
			.filter(type -> type.getKind() == TypeKind.DECLARED)
			.map(DeclaredType.class::cast)
			.map(DeclaredType::asElement)
			.map(TypeElement.class::cast)
			.filter(element -> !element.getQualifiedName().contentEquals(Entity.class.getName()))
			.forEach(element -> this.collectPotentialMemberMethods(element, members));
	}
	
	private boolean isPotentialMemberMethod(
		final ExecutableElement method,
		final Collection<ExecutableElement> methods)
	{
		return !method.isDefault()
			&& !method.getModifiers().contains(Modifier.STATIC)
			&& !this.isOverwritten(method, methods)
			&& !this.overridesObjectMethod(method);
	}
	
	private boolean isOverwritten(
		final ExecutableElement overridden,
		final Collection<ExecutableElement> methods)
	{
		final Elements elements = this.processingEnv.getElementUtils();
		return methods.stream()
			.filter(overrider -> overridden != overrider
				&& (elements.overrides(overrider, overridden, (TypeElement)overrider.getEnclosingElement())
					|| elements.overrides(overridden, overrider, (TypeElement)overridden.getEnclosingElement())))
			.findAny()
			.isPresent();
	}
	
	private boolean overridesObjectMethod(
		final ExecutableElement method)
	{
		final Elements elements = this.processingEnv.getElementUtils();
		return this.javaLangObjectMethods.stream()
			.filter(objectMethod -> elements.overrides(method, objectMethod, (TypeElement)method.getEnclosingElement()))
			.findAny()
			.isPresent();
	}
	
	private void validateMemberMethod(final ExecutableElement method)
	{
		if(method.getReturnType().getKind() == TypeKind.VOID
			|| method.getTypeParameters().size() > 0
			|| method.getParameters().size() > 0
			|| this.containsCheckedException(method.getThrownTypes()))
		{
			throw new EntityExceptionInvalidEntityMethod(method);
		}
	}
	
	private boolean containsCheckedException(final List<? extends TypeMirror> exceptionTypes)
	{
		return exceptionTypes.stream().anyMatch(this::isCheckedException);
	}
	
	private boolean isCheckedException(final TypeMirror exceptionType)
	{
		return !this.processingEnv.getTypeUtils().isAssignable(exceptionType, this.runtimeExceptionType);
	}
	
	private TypeMirror getTypeInEntity(
		final DeclaredType entityType,
		final ExecutableElement method)
	{
		final TypeMirror memberType = this.processingEnv.getTypeUtils().asMemberOf(entityType, method);
		return memberType instanceof ExecutableType
			? ((ExecutableType)memberType).getReturnType()
			: memberType;
	}
}
