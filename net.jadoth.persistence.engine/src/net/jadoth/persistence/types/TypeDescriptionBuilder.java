package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.BulkList;
import net.jadoth.functional.Aggregator;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleTypeIdOwner;
import net.jadoth.swizzling.types.SwizzleTypeLookup;
import net.jadoth.util.KeyValue;


public final class TypeDescriptionBuilder<M>
implements Aggregator<
	KeyValue<Class<?>, PersistenceTypeHandler<M, ?>>,
	BulkList<PersistenceTypeDefinition<?>>
>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final SwizzleTypeLookup                       typeLookup      ;
	private final BulkList<PersistenceTypeDefinition<?>> typeDescriptions = BulkList.New();



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public TypeDescriptionBuilder(final SwizzleTypeLookup typeLookup)
	{
		super();
		this.typeLookup = notNull(typeLookup);
	}

	@Override
	public void accept(final KeyValue<Class<?>, PersistenceTypeHandler<M, ?>> element)
	{
		final long typeId = this.typeLookup.lookupTypeId(element.key());

		/* if no typeId known by the local typeLookup, abort without exception.
		 * Rationale: there may be custom type handlers for native types but no defined native type Id for them.
		 * E.g. collection handlers.
		 * The intention is, that the description for those handlers is added later on not as a native type
		 * but as a dynamically encountered type (but with a native handler)
		 *
		 * Danger / potential downside: inconsistent type lookups for other cases (meaning not initializing
		 * custom native handlers but normal dynamically analyzed types later on) is not recognized here
		 * but swallowed. If this is a problem, another solution has to be found, maybe better seperation of the
		 * two concerns.
		 */
		if(typeId == Swizzle.nullId())
		{
			return; // type not known, abort
		}
		this.typeDescriptions.add(
			element.value().initializeTypeHandler(this.typeLookup)
		);
	}

	@Override
	public BulkList<PersistenceTypeDefinition<?>> yield()
	{
		return SwizzleTypeIdOwner.sortByTypeIdAscending(this.typeDescriptions);
	}

}
