package net.jadoth.meta;

import net.jadoth.meta.Field.FinalProperty;


public class CodeGeneratorEntity extends AbstractCodeGenerator<Field>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final FinalProperty Final(final String typeName, final String fieldName)
	{
		return new FinalProperty(typeName, fieldName, Code.DEFAULT, null, null);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public CodeGeneratorEntity(final String typeName, final Field... members)
	{
		this(typeName, null, members);
	}

	public CodeGeneratorEntity(final String typeName, final String superclass, final Field... members)
	{
		super(typeName, superclass, members);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////





	// testing
	public static void main(final String[] args)
	{
		final CodeGeneratorEntity generator = new CodeGeneratorEntity(
			"StorageDataConverterCsvConfiguration"     ,
			Final("CsvConfiguration", "csvConfiguration"),
			Final("XGettingMap<String, String>", "typeNameToCsvTypeNameMapping"),
			Final("XGettingMap<String, String>", "csvTypeNameToTypeNameMapping"),
			Final("String", "literalBooleanTrue"),
			Final("String", "literalBooleanFalse"),
			Final("char", "controlCharacterSeparator"),
			Final("String", "objectIdColumnName"),
			Final("String", "objectIdColumnTypeName"),
			Final("char", "literalListStarter"),
			Final("char", "literalListTerminator"),
			Final("char", "literalListSeparator")
		);

		System.out.println(generator.generateCode());
	}

}
