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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Supplier;

import one.microstream.X;
import one.microstream.collections.EqHashTable;
import one.microstream.hashing.XHashing;

public interface PersistenceMetaIdentifiers
{
	/**
	 * Identifiers cannot have whitespaces at the end or the beginning.
	 * A string that only consists of whitespaces is considered no identifier at all.
	 * 
	 * Note that for strings that don't have bordering whitespaces or are already {@code null},
	 * this method takes very little time and does not allocate any new instances. Only
	 * the problematic case is any mentionable effort.
	 * 
	 * @param s the raw string to be normalized.
	 * @return the normalized string, potentially {@code null}.
	 */
	public static String normalizeIdentifier(final String s)
	{
		if(s == null)
		{
			return null;
		}
		
		final String normalized = s.trim();
		
		return normalized.isEmpty()
			? null
			: normalized
		;
	}
	
	public static EqHashTable<String, Supplier<?>> defineConstantSuppliers()
	{
		final EqHashTable<String, Supplier<?>> entries = EqHashTable.New();
		
		// arbitrary constant identifiers that decouple constant resolving from class/field names.
		entries.add("XHashEqualator:hashEqualityValue"              , XHashing::hashEqualityValue              );
		entries.add("XHashEqualator:hashEqualityIdentity"           , XHashing::hashEqualityIdentity           );
		entries.add("XHashEqualator:keyValueHashEqualityKeyIdentity", XHashing::keyValueHashEqualityKeyIdentity);
		entries.add("XEmpty:Collection"                             , X::empty                                 );
		entries.add("XEmpty:Table"                                  , X::emptyTable                            );
		
		/*
		 * JDK constants (narrow selection of default-wise "worthy" for an entity graph)
		 * E.g. Iterators are not. See Persistence#unanalyzableTypes for a rationale on that.
		 * Also note:
		 * Collections#emptySortedSet is the same instance as Collections#emptyNavigableSet
		 * Collections#emptySortedMap is the same instance as Collections#emptyNavigableMap
		 * Arrays$NaturalOrder#NaturalOrder is unshared and only used by logic, not in data structures.
		 * MutableBigInteger does not have any shared instance constants.
		 */
		entries.add("JDK.Collections:emptyList"        , Collections::emptyList        ); // stateless!
		entries.add("JDK.Collections:emptySet"         , Collections::emptySet         ); // stateless!
		entries.add("JDK.Collections:emptyMap"         , Collections::emptyMap         ); // stateless!
		entries.add("JDK.Collections:emptyNavigableSet", Collections::emptyNavigableSet); // stateless!
		entries.add("JDK.Collections:emptyNavigableMap", Collections::emptyNavigableMap); // stateless!
//		Collections#emptySortedSet is the same instance as Collections#emptyNavigableSet
//		Collections#emptySortedMap is the same instance as Collections#emptyNavigableMap
		entries.add("JDK.Collections:reverseOrder"     , Collections::reverseOrder     ); // stateless!
		entries.add("JDK.Comparator:naturalOrder"      , Comparator::naturalOrder      ); // stateless!
		entries.add("JDK.BigDecimal:ZERO"              , () -> BigDecimal.ZERO         ); // no-op update()!
		entries.add("JDK.BigDecimal:ONE"               , () -> BigDecimal.ONE          ); // no-op update()!
		entries.add("JDK.BigDecimal:TEN"               , () -> BigDecimal.TEN          ); // no-op update()!
		entries.add("JDK.BigInteger:ZERO"              , () -> BigInteger.ZERO         ); // no-op update()!
		entries.add("JDK.BigInteger:ONE"               , () -> BigInteger.ONE          ); // no-op update()!
		entries.add("JDK.BigInteger:TEN"               , () -> BigInteger.TEN          ); // no-op update()!
		entries.add("JDK.Optional:empty"               , Optional::empty               ); // no-op update()!
		entries.add("JDK.OptionalInt:empty"            , OptionalInt::empty            ); // no-op update()!
		entries.add("JDK.OptionalLong:empty"           , OptionalLong::empty           ); // no-op update()!
		entries.add("JDK.OptionalDouble:empty"         , OptionalDouble::empty         ); // no-op update()!
				
		return entries;
	}
	
}
