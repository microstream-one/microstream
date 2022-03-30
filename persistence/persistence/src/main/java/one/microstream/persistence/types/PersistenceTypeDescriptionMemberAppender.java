package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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

	public void appendTypeMemberDescription(PersistenceTypeDescriptionMemberEnumConstant typeMember);
	
	
	
	public final class Default
	extends PersistenceTypeDictionary.Symbols
	implements PersistenceTypeDescriptionMemberAppender
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		// primitive definition special case char sequence
		private static final char[] PRIMITIVE_ = (KEYWORD_PRIMITIVE + ' ').toCharArray();
		private static final char[] ENUM_      = (KEYWORD_ENUM + ' ')     .toCharArray();



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final VarString vs;
		private final int       maxFieldTypeNameLength;
		private final int       maxDeclaringTypeNameLength;
		private final int       maxFieldNameLength;
		private final int       level;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final VarString vs                        ,
			final int       level                     ,
			final int       maxFieldTypeNameLength    ,
			final int       maxDeclaringTypeNameLength,
			final int       maxFieldNameLength
		)
		{
			super();
			this.vs                         =             vs                         ;
			this.level                      =             level                      ;
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

		@Override
		public void appendTypeMemberDescription(final PersistenceTypeDescriptionMemberEnumConstant typeMember)
		{
			this.vs.add(ENUM_).add(typeMember.name());
		}

	}


}
