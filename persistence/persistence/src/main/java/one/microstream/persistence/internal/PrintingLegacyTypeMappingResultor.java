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

import static one.microstream.X.notNull;

import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSet;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceLegacyTypeMapper;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResult;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResultor;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.util.similarity.MultiMatch;
import one.microstream.util.similarity.Similarity;

public class PrintingLegacyTypeMappingResultor<D> implements PersistenceLegacyTypeMappingResultor<D>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static String assembleMappingWithHeader(
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<?, ?>                                      result
	)
	{
		final VarString vs = VarString.New();
		assembleMappingHeader(vs, result);
		assembleMapping(vs, explicitMappings, matchedMembers, result);
		return vs.toString();
	}
	
	public static VarString assembleMappingHeader(
		final VarString                                vs    ,
		final PersistenceLegacyTypeMappingResult<?, ?> result
	)
	{
		vs
		.lf()
		.add("----------").lf()
		.add("Legacy type mapping required for legacy type ").lf()
		.add(result.legacyTypeDefinition().toTypeIdentifier()).lf()
		.add("to current type ").lf()
		.add(result.currentTypeHandler().toTypeIdentifier()).lf()
		.add("Fields:").lf()
		;
		
		return vs;
	}
	
	public static VarString assembleMapping(
		final VarString                                                                     vs              ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<?, ?>                                      result
	)
	{
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers =
			result.currentToLegacyMembers()
		;
		final XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers = result.newCurrentMembers();
		
		// main mapping loop in current type's declared order
		for(final PersistenceTypeDefinitionMember currentMember : result.currentTypeHandler().membersInDeclaredOrder())
		{
			final Similarity<PersistenceTypeDefinitionMember> legacyMember = currentToLegacyMembers.get(currentMember);
			if(legacyMember != null)
			{
				assembleMemberName(vs, legacyMember.sourceElement()).tab(); // old name column
				assembleTokenMappedMember(vs, legacyMember).blank(); // translator token
				assembleMemberName(vs, currentMember).lf(); // new name column
			}
			else if(newCurrentMembers.contains(currentMember))
			{
				vs.tab(); // empty old name column
				assembleTokenNewMember(vs).blank(); // translator token
				assembleMemberName(vs, currentMember).lf(); // new name
			}
			else
			{
				throw new PersistenceException("Inconsistent current type member mapping: " + currentMember.identifier());
			}
		}
		
		// discarded legacy members are added at the end
		for(final PersistenceTypeDefinitionMember e : result.discardedLegacyMembers())
		{
			assembleMemberName(vs, e).tab(); // old name
			assembleTokenDiscardedMember(vs); // translator token
			vs.lf(); // no new name column at all
		}
		
		return vs;
	}
	
	static final VarString assembleTokenMappedMember(
		final VarString vs,
		final Similarity<PersistenceTypeDefinitionMember> mappedLegacyMember
	)
	{
		return vs
		.add('-')
		.padRight(
			PersistenceLegacyTypeMapper.similarityToString(mappedLegacyMember),
			PersistenceLegacyTypeMapper.Defaults.defaultMappingTokenBaseLength(),
			'-'
		)
		.add("->")
		;
	}
	
	static final VarString assembleTokenNewMember(final VarString vs)
	{
		return vs.add(PersistenceLegacyTypeMapper.Defaults.defaultNewMemberString());
	}
	
	static final VarString assembleTokenDiscardedMember(final VarString vs)
	{
		return vs.add(PersistenceLegacyTypeMapper.Defaults.defaultDiscardedMemberString());
	}
	
	public static final VarString assembleMemberName(final VarString vs, final PersistenceTypeDefinitionMember member)
	{
		return vs.add(member.typeName()).blank().add(member.identifier());
	}
	
	public static <D> PrintingLegacyTypeMappingResultor<D> New(
		final PersistenceLegacyTypeMappingResultor<D> delegate
	)
	{
		return new PrintingLegacyTypeMappingResultor<>(
			notNull(delegate)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final PersistenceLegacyTypeMappingResultor<D> delegate;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	PrintingLegacyTypeMappingResultor(final PersistenceLegacyTypeMappingResultor<D> delegate)
	{
		super();
		this.delegate = delegate;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public <T> PersistenceLegacyTypeMappingResult<D, T> createMappingResult(
		final PersistenceTypeDefinition                                                     legacyTypeDefinition,
		final PersistenceTypeHandler<D, T>                                                  currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
		final XGettingSet<PersistenceTypeDefinitionMember>                                  explicitNewMembers  ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		final PersistenceLegacyTypeMappingResult<D, T> result = this.delegate.createMappingResult(
			legacyTypeDefinition, currentTypeHandler, explicitMappings, explicitNewMembers, matchedMembers
		);
		
		final String output = assembleMappingWithHeader(explicitMappings, matchedMembers, result);
		System.out.println(output);
		
		return result;
	}
	
}
