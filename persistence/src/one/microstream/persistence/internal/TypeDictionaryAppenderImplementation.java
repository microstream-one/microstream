package one.microstream.persistence.internal;

import static one.microstream.math.XMath.notNegative;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberField;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberPrimitiveDefinition;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldComplex;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldSimple;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldVariableLength;
import one.microstream.persistence.types.PersistenceTypeDictionary;

public final class TypeDictionaryAppenderImplementation
extends PersistenceTypeDictionary.Symbols
implements PersistenceTypeDescriptionMember.Appender
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// equal-length predefined char sequences
//	private static final char[] static_final_  = (KEYWORD_STATIC+' '+KEYWORD_FINAL+' ').toCharArray();
//	private static final char[] static_______  = (KEYWORD_STATIC + "       ")          .toCharArray();
//	private static final char[] INSTANCE_FIELD = "             "                       .toCharArray();

	// primitive definition special case char sequence
	private static final char[] PRIMITIVE_     = (KEYWORD_PRIMITIVE + ' ')             .toCharArray();



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final VarString vc;
	private final int maxFieldTypeNameLength    ;
	private final int maxDeclaringTypeNameLength;
	private final int maxFieldNameLength        ;
	private final int level;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public TypeDictionaryAppenderImplementation(
		final VarString vc,
		final int level,
		final int maxFieldTypeNameLength,
		final int maxDeclaringTypeNameLength,
		final int maxFieldNameLength
	)
	{
		super();
		this.vc = vc;
		this.level = level;
		this.maxFieldTypeNameLength     = notNegative(maxFieldTypeNameLength    );
		this.maxDeclaringTypeNameLength = notNegative(maxDeclaringTypeNameLength);
		this.maxFieldNameLength         = notNegative(maxFieldNameLength        );
	}

	private void indentMember()
	{
		this.vc.repeat(this.level, '\t');
	}

	private void terminateMember()
	{
		this.vc.add(MEMBER_TERMINATOR).lf();
	}

	private void appendField(final PersistenceTypeDescriptionMemberField member)
	{
		PersistenceTypeDictionary.paddedFullQualifiedFieldName(
			this.vc.padRight(member.typeName(), this.maxFieldTypeNameLength, ' ').blank(),
			member.declaringTypeName(),
			this.maxDeclaringTypeNameLength,
			member.name(),
			this.maxFieldNameLength
		);
	}

	private void appendPseudoField(final PersistenceTypeDescriptionMemberPseudoField member)
	{
		this.vc
		.padRight(member.typeName(), this.maxFieldTypeNameLength, ' ').blank()
		.padRight(member.name()    , this.maxFieldNameLength    , ' ')
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final PersistenceTypeDescriptionMember e)
	{
		this.indentMember();
		e.assembleTypeDescription(this);
		this.terminateMember();
	}

	@Override
	public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberField typeMember)
	{
		this.appendField(typeMember);
	}

	@Override
	public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberPseudoFieldSimple typeMember)
	{
		this.appendPseudoField(typeMember);
	}

	@Override
	public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberPseudoFieldVariableLength typeMember)
	{
		this.appendPseudoField(typeMember);
	}

	@Override
	public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberPseudoFieldComplex typeMember)
	{
		this.appendPseudoField(typeMember);
		this.vc.add(MEMBER_COMPLEX_DEF_START).lf();
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> members = typeMember.members();
		final TypeDictionaryAppenderImplementation appender = members.iterate(
			new TypeDictionaryAppenderBuilder(this.vc, this.level + 1)
		).yield();
		members.iterate(appender);
		this.indentMember();
		this.vc.add(MEMBER_COMPLEX_DEF_END);
	}

	@Override
	public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberPrimitiveDefinition typeMember)
	{
		this.vc.add(PRIMITIVE_).add(typeMember.primitiveDefinition());
	}

}
