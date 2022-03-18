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

import one.microstream.equality.Equalator;
import one.microstream.typing.TypeMappingLookup;
import one.microstream.util.similarity.MatchValidator;
import one.microstream.util.similarity.Similator;

//@FunctionalInterface - well, lol.
public interface PersistenceMemberMatchingProvider
{
	public default Equalator<PersistenceTypeDefinitionMember> provideMemberMatchingEqualator()
	{
		// optional, null by default.
		return null;
	}
	
	public default Similator<PersistenceTypeDefinitionMember> provideMemberMatchingSimilator(
		final TypeMappingLookup<Float> typeSimilarity
	)
	{
		return PersistenceMemberSimilator.New(typeSimilarity);
	}
	
	public default MatchValidator<PersistenceTypeDefinitionMember> provideMemberMatchValidator()
	{
		// optional, null by default.
		return null;
	}
	
	
	
	public static PersistenceMemberMatchingProvider New()
	{
		return new PersistenceMemberMatchingProvider.Default();
	}
	
	public class Default implements PersistenceMemberMatchingProvider
	{
		// since default methods, the ability to instantiate stateless instances from interfaces is missing
	}
	
}
