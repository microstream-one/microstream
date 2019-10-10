
package one.microstream.entity.codegen;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;

import one.microstream.entity.EntityData;


class DataSourceFile extends SourceFile
{
	final static String SUFFIX = "Data";
	
	DataSourceFile(
		final ProcessingEnvironment processingEnv,
		final TypeElement entityTypeElement,
		final List<Member> members)
	{
		super(
			processingEnv,
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
				.tab().add("public ").add(m.typeName).blank().add(m.methodName).add("()").newline()
				.tab().add("{").newline()
				.tab(2).add("return this.").add(m.name).add(";").newline()
				.tab().add("}"));
		
		this.newline().add("}");
	}
}
