package one.microstream.persistence.internal;

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

import one.microstream.chars.VarString;
import one.microstream.functional.Aggregator;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberAppender;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldReflective;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;

public final class TypeDictionaryAppenderBuilder
implements Aggregator<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMemberAppender>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final VarString vc   ;
	private final int       level;

	int maxFieldTypeNameLength    ;
	int maxDeclaringTypeNameLength;
	int maxFieldNameLength        ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public TypeDictionaryAppenderBuilder(final VarString vc, final int level)
	{
		super();
		this.vc    = vc   ;
		this.level = level;
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

	private void measureFieldStrings(final PersistenceTypeDescriptionMemberFieldReflective member)
	{
		this.measureTypeName         (member.typeName());
		this.measureDeclaringTypeName(member.declaringTypeName());
		this.measureFieldName        (member.name());
	}

	private void measureGenericFieldStrings(final PersistenceTypeDescriptionMemberFieldGeneric member)
	{
		this.measureTypeName (member.typeName());
		this.measureFieldName(member.name());
	}

	@Override
	public final void accept(final PersistenceTypeDescriptionMember member)
	{
		// (21.03.2013 TM)XXX: type dictionary member field measurement uses awkward instanceoffing
		if(member instanceof PersistenceTypeDescriptionMemberFieldReflective)
		{
			this.measureFieldStrings((PersistenceTypeDescriptionMemberFieldReflective)member);
		}
		else if(member instanceof PersistenceTypeDescriptionMemberFieldGeneric)
		{
			this.measureGenericFieldStrings((PersistenceTypeDescriptionMemberFieldGeneric)member);
		}
		// otherwise, leave all lengths at 0 (e.g. primitive definition)
	}

	@Override
	public final PersistenceTypeDescriptionMemberAppender yield()
	{
		return new PersistenceTypeDescriptionMemberAppender.Default(
			this.vc,
			this.level,
			this.maxFieldTypeNameLength,
			this.maxDeclaringTypeNameLength,
			this.maxFieldNameLength
		);
	}
}
