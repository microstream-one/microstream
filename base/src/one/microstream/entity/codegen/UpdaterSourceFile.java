
package one.microstream.entity.codegen;

import java.beans.Introspector;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;

import one.microstream.entity.Entity;


class UpdaterSourceFile extends SourceFile
{
	final static String SUFFIX = "Updater";
	
	UpdaterSourceFile(
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
		final List<? extends TypeParameterElement> typeParameters               =
			this.entityTypeElement.getTypeParameters();
		final String                               typeParametersDeclCode       =
			this.createTypeParameterDeclCode(typeParameters);
		final String                               typeParametersNameCode       =
			this.createTypeParameterNameCode(typeParameters);
		final String                               methodTypeParametersDeclCode = typeParametersDeclCode.isEmpty()
			? ""
			: typeParametersDeclCode.concat(" ");
		final String                               varName                      =
			Introspector.decapitalize(this.entityName);
		
		this.add("public class ").add(this.typeName).newline().add("{");
		
		// methods
		this.members.forEach(
			m -> this.newline()
				.tab().add("public static ").add(methodTypeParametersDeclCode).add("boolean ")
				.add(m.setterName).add("(final ").add(this.entityName).add(typeParametersNameCode)
				.blank().add(varName).add(", final ").add(m.typeName).blank().add(m.name).add(")").newline()
				.tab().add("{").newline()
				.tab(2).add("return ").add(this.addImport(Entity.class)).add(".updateData(").newline()
				.tab(3).add("").add(varName).add(",").newline()
				.tab(3).add("").add(this.getGeneratedTypeName(CreatorSourceFile.SUFFIX)).add(".New(").add(varName)
				.add(").").add(m.name).add("(").add(m.name).add(").createData());").newline()
				.tab().add("}").newline());
		
		// constructor
		this.newline().tab().add("protected ").add(this.typeName).add("()").newline()
			.tab().add("{").newline()
			.tab(2).add("super();").newline()
			.tab().add("}").newline();
		
		this.add("}");
	}
}
