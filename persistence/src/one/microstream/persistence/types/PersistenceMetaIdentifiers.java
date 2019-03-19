package one.microstream.persistence.types;

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
	 * Note that for strings that don't have bordering whitespaces or are already <code>null</code>,
	 * this method takes very little time and does not allocate any new instances. Only
	 * the problematic case is any mentionable effort.
	 * 
	 * @param s the raw string to be normalized.
	 * @return the normalized string, potentially <code>null</code>.
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
		
		return entries;
	}
}
