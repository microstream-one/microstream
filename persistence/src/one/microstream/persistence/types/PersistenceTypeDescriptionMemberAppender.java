package one.microstream.persistence.types;

import static one.microstream.math.XMath.notNegative;

import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.internal.TypeDictionaryAppenderBuilder;
import one.microstream.persistence.types.PersistenceTypeDictionary.Symbols;


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

		private final VarString vs;
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
			this.vs = vc;
			this.level = level;
			this.maxFieldTypeNameLength     = notNegative(maxFieldTypeNameLength    );
			this.maxDeclaringTypeNameLength = notNegative(maxDeclaringTypeNameLength);
			this.maxFieldNameLength         = notNegative(maxFieldNameLength        );
		}

		private void indentMember()
		{
			this.vs.repeat(this.level, '\t');
		}

		private void terminateMember()
		{
			this.vs.add(MEMBER_TERMINATOR).lf();
		}

		private void appendField(final PersistenceTypeDescriptionMemberField member)
		{
			// field type name gets assembled in any case
			this.vs.padRight(member.typeName(), this.maxFieldTypeNameLength, ' ').blank();
			
			// field qualifier (e.g. declaring type name) is optional
			final String qualifier = member.qualifier();
			if(qualifier != null)
			{
				this.vs
				.padRight(qualifier, this.maxDeclaringTypeNameLength, ' ')
				.add(Symbols.MEMBER_FIELD_QUALIFIER_SEPERATOR)
				;
			}
			
			this.vs.padRight(member.name(), this.maxFieldNameLength, ' ');
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
			this.appendField(typeMember);
		}

		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberFieldGenericComplex typeMember)
		{
			this.appendField(typeMember);
			this.vs.add(MEMBER_COMPLEX_DEF_START).lf();
			final XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> members = typeMember.members();
			final PersistenceTypeDescriptionMemberAppender appender = members.iterate(
				new TypeDictionaryAppenderBuilder(this.vs, this.level + 1)
			).yield();
			members.iterate(appender);
			this.indentMember();
			this.vs.add(MEMBER_COMPLEX_DEF_END);
		}

		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberPrimitiveDefinition typeMember)
		{
			this.vs.add(PRIMITIVE_).add(typeMember.primitiveDefinition());
		}

	}


}