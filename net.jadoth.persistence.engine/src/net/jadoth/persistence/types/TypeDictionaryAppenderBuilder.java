package net.jadoth.persistence.types;

import net.jadoth.chars.VarString;
import net.jadoth.functional.Aggregator;

final class TypeDictionaryAppenderBuilder
implements Aggregator<PersistenceTypeDescriptionMember, TypeDictionaryAppenderImplementation>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final VarString vc   ;
	private final int       level;

	int maxFieldTypeNameLength    ;
	int maxDeclaringTypeNameLength;
	int maxFieldNameLength        ;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public TypeDictionaryAppenderBuilder(final VarString vc, final int level)
	{
		super();
		this.vc             = vc            ;
		this.level          = level         ;
	}


	private void measureTypeName(final String typeName)
	{
		if(typeName.length() > this.maxFieldTypeNameLength)
		{
			this.maxFieldTypeNameLength = typeName.length();
		}
	}

	private void measureDeclaringTypeName(final String declaringTypeName)
	{
		if(declaringTypeName.length() > this.maxDeclaringTypeNameLength)
		{
			this.maxDeclaringTypeNameLength = declaringTypeName.length();
		}
	}

	private void measureFieldName(final String fieldName)
	{
		if(fieldName.length() > this.maxFieldNameLength)
		{
			this.maxFieldNameLength = fieldName.length();
		}
	}

	private void measureFieldStrings(final PersistenceTypeDescriptionMemberField member)
	{
		this.measureTypeName         (member.typeName());
		this.measureDeclaringTypeName(member.declaringTypeName());
		this.measureFieldName        (member.name());
	}

	private void measurePseudoFieldStrings(final PersistenceTypeDescriptionMemberPseudoField member)
	{
		this.measureTypeName (member.typeName());
		this.measureFieldName(member.name());
	}

	@Override
	public final void accept(final PersistenceTypeDescriptionMember member)
	{
		// (21.03.2013)XXX: type dictionary member field measurement uses awkward instanceoffing
		if(member instanceof PersistenceTypeDescriptionMemberField)
		{
			this.measureFieldStrings((PersistenceTypeDescriptionMemberField)member);
		}
		else if(member instanceof PersistenceTypeDescriptionMemberPseudoField)
		{
			this.measurePseudoFieldStrings((PersistenceTypeDescriptionMemberPseudoField)member);
		}
		// otherwise, leave all lengths at 0 (e.g. primitive definition)
	}

	@Override
	public final TypeDictionaryAppenderImplementation yield()
	{
		return new TypeDictionaryAppenderImplementation(
			this.vc,
			this.level,
			this.maxFieldTypeNameLength,
			this.maxDeclaringTypeNameLength,
			this.maxFieldNameLength
		);
	}
}
