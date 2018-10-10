package net.jadoth.persistence.internal;

import static net.jadoth.X.notNull;

import java.util.Scanner;

import net.jadoth.chars.VarString;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResult;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResultor;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMember;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.util.matching.MultiMatch;

public class InquiringLegacyTypeMappingResultor<M> implements PersistenceLegacyTypeMappingResultor<M>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <M> InquiringLegacyTypeMappingResultor<M> New(
		final PersistenceLegacyTypeMappingResultor<M> delegate
	)
	{
		return new InquiringLegacyTypeMappingResultor<>(
			notNull(delegate)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceLegacyTypeMappingResultor<M> delegate;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	InquiringLegacyTypeMappingResultor(final PersistenceLegacyTypeMappingResultor<M> delegate)
	{
		super();
		this.delegate = delegate;
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public <T> PersistenceLegacyTypeMappingResult<M, T> createMappingResult(
		final PersistenceTypeDefinition                                                     legacyTypeDefinition,
		final PersistenceTypeHandler<M, T>                                                  currentTypeHandler  ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings    ,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers
	)
	{
		final PersistenceLegacyTypeMappingResult<M, T> result = this.delegate.createMappingResult(
			legacyTypeDefinition,
			currentTypeHandler  ,
			explicitMappings    ,
			matchedMembers
		);
		
		if(this.inquireApproval(explicitMappings, matchedMembers, result))
		{
			return result;
		}
		
		// (10.10.2018 TM)EXCP: proper exception
		throw new RuntimeException(
			"User-aborted legacy type mapping for type " + legacyTypeDefinition.toTypeIdentifier()
		);
	}
		
	protected boolean inquireApproval(
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<M, ?>                                      result
	)
	{
		System.out.println(this.buildInquiry(explicitMappings, matchedMembers, result));
		
		@SuppressWarnings("resource") // the bloody scanner would auto-close System.in. Hilarious JDK code again.
		final Scanner scanner = new Scanner(System.in);
		
		final String inputLine = scanner.nextLine();
		if('y' == Character.toLowerCase(inputLine.trim().charAt(0)))
		{
			return true;
		}
		
		return false;
	}
	
	protected String buildInquiry(
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<M, ?>                                      result
	)
	{
		// intentionally no instance field to not permanently occupy memory by an initializer part.
		final VarString vs = VarString.New(1000);
		this.assembleInquiryStart(vs, result);
		this.assembleMapping(vs, explicitMappings, matchedMembers, result);
		this.assembleInquiryEnd(vs, result);
		final String inquiry = vs.toString();
		return inquiry;
	}
	
	protected VarString assembleInquiryStart(
		final VarString                                vs    ,
		final PersistenceLegacyTypeMappingResult<M, ?> result
	)
	{
		vs.add(result.legacyTypeDefinition().toTypeIdentifier()).lf();
		
		return vs;
	}
	
	protected VarString assembleMapping(
		final VarString                                                                     vs              ,
		final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> explicitMappings,
		final MultiMatch<PersistenceTypeDefinitionMember>                                   matchedMembers  ,
		final PersistenceLegacyTypeMappingResult<M, ?>                                      result
	)
	{
		// (10.10.2018 TM)FIXME: OGS-3: print mapping
		vs.add("old ---magic--> current");
		
		return vs;
	}
	
	protected VarString assembleInquiryEnd(
		final VarString                                vs    ,
		final PersistenceLegacyTypeMappingResult<M, ?> result
	)
	{
		vs.lf().add("You like? (y/n)");
		
		return vs;
	}
	
}
