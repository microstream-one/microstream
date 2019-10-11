
package one.microstream.wrapping.codegen;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import one.microstream.wrapping.GenerateWrapper;


public class WrapperProcessor extends AbstractProcessor
{
	private final static String OPTION_TYPES = "microstream.wrapper.types";
	
	private Set<String>         types;
	
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
	
	private void generateWrapper(final TypeElement typeElem)
	{
		System.out.println(typeElem);
	}
}
