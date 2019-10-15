
package one.microstream.entity.codegen;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayerIdentity;


class CreatorSourceFile extends SourceFile
{
	final static String SUFFIX = "Creator";
	
	CreatorSourceFile(
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
		final String                               optionalDiamond              = typeParametersNameCode.isEmpty()
			? ""
			: "<>";
		
		this.add("public interface ").add(this.typeName).add(typeParametersDeclCode)
			.add(" extends ").add(this.addImport(Entity.class)).add(".Creator<")
			.add(this.entityName).add(typeParametersNameCode).add(", ").add(this.typeName).add(typeParametersNameCode)
			.add(">")
			.newline().add("{");
		
		// methods
		this.members.forEach(
			m -> this.newline()
				.tab().add("public ").add(this.typeName).add(typeParametersNameCode).blank()
				.add(m.name).add("(").add(m.typeName).blank().add(m.name).add(")")
				.add(m.throwsClause).add(";").newline());
		
		// pseudo constructors
		this.newline().tab().add("public static ").add(methodTypeParametersDeclCode)
			.add(this.typeName).add(typeParametersNameCode).add(" New()").newline()
			.tab().add("{").newline()
			.tab(2).add("return new Default").add(optionalDiamond).add("();").newline()
			.tab().add("}").newline();
		this.newline().tab().add("public static ").add(methodTypeParametersDeclCode)
			.add(this.typeName).add(typeParametersNameCode).add(" New(final ")
			.add(this.entityName).add(typeParametersNameCode).add(" other)").newline()
			.tab().add("{").newline()
			.tab(2).add("return new Default").add(typeParametersNameCode).add("().copy(other);").newline()
			.tab().add("}").newline();
		
		// default implementation class
		
		this.newline().tab().add("public class Default").add(typeParametersDeclCode).newline()
			.tab(2).add("extends Entity.Creator.Abstract<")
			.add(this.entityName).add(typeParametersNameCode).add(", ")
			.add(this.typeName).add(typeParametersNameCode).add(">").newline()
			.tab(2).add("implements ").add(this.typeName).add(typeParametersNameCode).newline()
			.tab().add("{").newline();
		
		// fields
		this.members.forEach(
			m -> this.tab(2).add("private ").add(m.paddedTypeName).blank().add(m.paddedName).add(";").newline());
		
		// constructor
		this.newline()
			.tab(2).add("protected Default()").newline()
			.tab(2).add("{").newline()
			.tab(3).add("super();").newline()
			.tab(2).add("}");
		
		// setter methods
		this.members.forEach(
			m -> this.newline().newline()
				.tab(2).add("@Override").newline()
				.tab(2).add("public ").add(this.typeName).add(typeParametersNameCode).blank()
				.add(m.name).add("(final ").add(m.typeName).blank().add(m.name).add(")")
				.add(m.throwsClause).newline()
				.tab(2).add("{").newline()
				.tab(3).add("this.").add(m.name).add(" = ").add(m.name).add(";").newline()
				.tab(3).add("return this;").newline()
				.tab(2).add("}"));
		
		// override createEntityInstance()
		this.newline().newline()
			.tab(2).add("@Override").newline()
			.tab(2).add("protected ").add(this.addImport(EntityLayerIdentity.class))
			.add(" createEntityInstance()").newline()
			.tab(2).add("{").newline()
			.tab(3).add("return new ").add(this.getGeneratedTypeName(IdentitySourceFile.SUFFIX))
			.add(optionalDiamond).add("();").newline()
			.tab(2).add("}");
		
		// override createData(entityInstance)
		this.newline().newline()
			.tab(2).add("@Override").newline()
			.tab(2).add("public ").add(this.entityName).add(typeParametersNameCode).add(" createData(final ")
			.add(this.entityName).add(typeParametersNameCode).add(" entityInstance)").newline()
			.tab(2).add("{").newline()
			.tab(3).add("return new ").add(this.getGeneratedTypeName(DataSourceFile.SUFFIX))
			.add(optionalDiamond).add("(entityInstance");
		this.members.forEach(m -> this.add(",").newline().tab(4).add("this.").add(m.paddedName));
		this.add(");").newline()
			.tab(2).add("}");
		
		// override copy(other)
		this.newline().newline()
			.tab(2).add("@Override").newline()
			.tab(2).add("public ").add(this.typeName).add(typeParametersNameCode).add(" copy(final ")
			.add(this.entityName).add(typeParametersNameCode).add(" other)").newline()
			.tab(2).add("{").newline()
			.tab(3).add("final ").add(this.entityName).add(typeParametersNameCode).add(" data = Entity.data(other);");
		this.members.forEach(
			m -> this.newline().tab(3).add("this.").add(m.paddedName).add(" = data.").add(m.paddedMethodName)
				.add("();"));
		this.newline().tab(3).add("return this;").newline()
			.tab(2).add("}");
		
		this.newline().tab().add("}");
		this.newline().add("}");
	}
}
