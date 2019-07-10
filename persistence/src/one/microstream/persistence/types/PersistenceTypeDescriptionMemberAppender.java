package one.microstream.persistence.types;

import static one.microstream.math.XMath.notNegative;

import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.internal.TypeDictionaryAppenderBuilder;


public interface PersistenceTypeDescriptionMemberAppender extends Consumer<PersistenceTypeDescriptionMember>
{
	@Override
	public void accept(PersistenceTypeDescriptionMember typeMember);
	
	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberField typeMember);

	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberFieldGenericVariableLength typeMember);

	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberFieldGenericComplex typeMember);

	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberPrimitiveDefinition typeMember);
	
	
	
	public final class Default
	extends PersistenceTypeDictionary.Symbols
	implements PersistenceTypeDescriptionMemberAppender
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		// equal-length predefined char sequences
//		private static final char[] static_final_  = (KEYWORD_STATIC+' '+KEYWORD_FINAL+' ').toCharArray();
//		private static final char[] static_______  = (KEYWORD_STATIC + "       ")          .toCharArray();
//		private static final char[] INSTANCE_FIELD = "             "                       .toCharArray();

		// primitive definition special case char sequence
		private static final char[] PRIMITIVE_     = (KEYWORD_PRIMITIVE + ' ')             .toCharArray();



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final VarString vc;
		private final int maxFieldTypeNameLength    ;
		private final int maxDeclaringTypeNameLength;
		private final int maxFieldNameLength        ;
		private final int level;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
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
				member.qualifier(),
				this.maxDeclaringTypeNameLength,
				member.name(),
				this.maxFieldNameLength
			);
		}

		private void appendGenericField(final PersistenceTypeDescriptionMemberFieldGeneric member)
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
		public final void accept(final PersistenceTypeDescriptionMember typeMember)
		{
			this.indentMember();
			typeMember.assembleTypeDescription(this);
			this.terminateMember();
		}
		
		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberField typeMember)
		{
			this.appendField(typeMember);
		}

		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberFieldGenericVariableLength typeMember)
		{
			this.appendGenericField(typeMember);
		}

		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberFieldGenericComplex typeMember)
		{
			this.appendGenericField(typeMember);
			this.vc.add(MEMBER_COMPLEX_DEF_START).lf();
			final XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> members = typeMember.members();
			final PersistenceTypeDescriptionMemberAppender appender = members.iterate(
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


}