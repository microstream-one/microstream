
package one.microstream.wrapping.codegen;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import one.microstream.wrapping.GenerateWrapper;

/**
 * 
 * @author FH
 */
public class WrapperProcessor extends AbstractProcessor
{
	private final static String     OPTION_TYPES = "microstream.wrapper.types";
	
	private Set<String>             additionalTypes;
	private List<ExecutableElement> javaLangObjectMethods;
	private boolean                 processed    = false;
	
	public WrapperProcessor()
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
		 * 'Hack' to process all elements, not only annotated classes.
		 */
		return Collections.singleton("*");
	}
	
	@Override
	public Set<String> getSupportedOptions()
	{
		return Collections.singleton(OPTION_TYPES);
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
		
		final String option = processingEnv.getOptions().get(OPTION_TYPES);
		if(option != null && option.length() > 0)
		{
			this.additionalTypes = Arrays.stream(option.split(","))
				.map(String::trim)
				.collect(Collectors.toSet());
		}
	}
	
	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv)
	{
		if(roundEnv.processingOver() || this.processed)
		{
			return false;
		}
		
		// annotated types
		final Map<String, TypeElement> types = roundEnv.getElementsAnnotatedWith(GenerateWrapper.class).stream()
			.filter(e -> e.getKind() == ElementKind.INTERFACE)
			.map(TypeElement.class::cast)
			.collect(Collectors.toMap(t -> t.getQualifiedName().toString(), t -> t));
		
		// additional types
		if(this.additionalTypes != null)
		{
			this.additionalTypes.stream()
				.filter(name -> !types.containsKey(name))
				.map(this.processingEnv.getElementUtils()::getTypeElement)
				.filter(Objects::nonNull)
				.forEach(t -> types.put(t.getQualifiedName().toString(), t));
		}
		
		types.values().forEach(this::generateWrapper);
		
		this.processed = true;
		
		return false;
	}
	
	private void generateWrapper(final TypeElement typeElement)
	{
		final Set<ExecutableElement> methods = new LinkedHashSet<>();
		this.collectMethods(typeElement, methods);
		new WrapperTypeGenerator(this.processingEnv, typeElement, methods).generateType();
	}
	
	private void collectMethods(
		final TypeElement typeElement,
		final Set<ExecutableElement> methods)
	{
		typeElement.getEnclosedElements().stream()
			.filter(e -> e.getKind() == ElementKind.METHOD)
			.map(ExecutableElement.class::cast)
			.filter(method -> this.filter(method, methods))
			.forEach(methods::add);
		
		typeElement.getInterfaces().stream()
			.filter(type -> type.getKind() == TypeKind.DECLARED)
			.map(DeclaredType.class::cast)
			.map(DeclaredType::asElement)
			.map(TypeElement.class::cast)
			.forEach(element -> this.collectMethods(element, methods));
	}
	
	private boolean filter(
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
}
