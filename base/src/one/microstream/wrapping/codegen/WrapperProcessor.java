
package one.microstream.wrapping.codegen;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

import one.microstream.wrapping.GenerateWrapper;


public class WrapperProcessor extends AbstractProcessor
{
	private final static String     OPTION_TYPES = "microstream.wrapper.types";
	
	private Set<String>             types;
	private List<ExecutableElement> javaLangObjectMethods;
	
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
		
		final String option = processingEnv.getOptions().get(OPTION_TYPES);
		if(option != null && option.length() > 0)
		{
			this.types = Arrays.stream(option.split(",")).collect(Collectors.toSet());
		}
	}
	
	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv)
	{
		if(roundEnv.processingOver())
		{
			return false;
		}
		
		roundEnv.getRootElements().stream()
			.filter(e -> e.getKind() == ElementKind.INTERFACE)
			.map(TypeElement.class::cast)
			.filter(this::accept)
			.forEach(this::generateWrapper);
		
		return true;
	}
	
	private boolean accept(final TypeElement typeElem)
	{
		return typeElem.getAnnotation(GenerateWrapper.class) != null
			|| (this.types != null && this.types.contains(typeElem.getQualifiedName().toString()));
	}
	
	private void generateWrapper(final TypeElement typeElement)
	{
		final Set<ExecutableElement>  methods               = new LinkedHashSet<>();
		final List<ExecutableElement> javaLangObjectMethods = this.getJavaLangObjectMethods(typeElement);
		this.collectMethods(typeElement, methods, javaLangObjectMethods);
		new WrapperSourceFile(this.processingEnv, typeElement, methods).generateType();
	}
	
	public List<ExecutableElement> getJavaLangObjectMethods(final TypeElement typeElement)
	{
		if(this.javaLangObjectMethods == null)
		{
			final TypeElement javaLangObject = (TypeElement)((DeclaredType)this.processingEnv.getTypeUtils()
				.directSupertypes(typeElement.asType()).get(0)).asElement();
			this.javaLangObjectMethods = javaLangObject.getEnclosedElements().stream()
				.filter(e -> e.getKind() == ElementKind.METHOD)
				.map(ExecutableElement.class::cast)
				.filter(method -> !method.getModifiers().contains(Modifier.STATIC))
				.collect(Collectors.toList());
		}
		return this.javaLangObjectMethods;
	}
	
	private void collectMethods(
		final TypeElement typeElement,
		final Set<ExecutableElement> methods,
		final List<ExecutableElement> javaLangObjectMethods)
	{
		typeElement.getEnclosedElements().stream()
			.filter(e -> e.getKind() == ElementKind.METHOD)
			.map(ExecutableElement.class::cast)
			.filter(method -> this.filter(method, methods, javaLangObjectMethods))
			.forEach(methods::add);
		
		typeElement.getInterfaces().stream()
			.filter(type -> type.getKind() == TypeKind.DECLARED)
			.map(DeclaredType.class::cast)
			.map(DeclaredType::asElement)
			.map(TypeElement.class::cast)
			.forEach(element -> this.collectMethods(element, methods, javaLangObjectMethods));
	}
	
	private boolean filter(
		final ExecutableElement method,
		final Collection<ExecutableElement> methods,
		final Collection<ExecutableElement> javaLangObjectMethods)
	{
		return !method.isDefault()
			&& !method.getModifiers().contains(Modifier.STATIC)
			&& !this.isOverwritten(method, methods)
			&& !this.overridesObjectMethod(method, javaLangObjectMethods);
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
		final ExecutableElement method,
		final Collection<ExecutableElement> javaLangObjectMethods)
	{
		final Elements elements = this.processingEnv.getElementUtils();
		return javaLangObjectMethods.stream()
			.filter(objectMethod -> elements.overrides(method, objectMethod, (TypeElement)method.getEnclosingElement()))
			.findAny()
			.isPresent();
	}
}
