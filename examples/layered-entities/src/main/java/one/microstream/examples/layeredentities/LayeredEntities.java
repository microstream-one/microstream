package one.microstream.examples.layeredentities;

import one.microstream.collections.types.XGettingTable;
import one.microstream.entity.Entity;
import one.microstream.entity.EntityVersionContext;
import one.microstream.examples.layeredentities._Human.HumanUpdater;

public class LayeredEntities
{
	public static void main(final String[] args)
	{
		final Human human = EntityFactory.HumanCreator()
			.name("John Doe")
			.address(
				EntityFactory.AddressCreator()
					.street("Main Street")
					.city("Springfield")
					.create()
			)
			.create();
		
		HumanUpdater.setAddress(
			human,
			EntityFactory.AddressCreator()
				.street("Rose Boulevard")
				.city("Newtown")
				.create()
		);
		
		printVersions(human);
	}

	static void printVersions(final Entity entity)
	{
		final EntityVersionContext<Integer>  context  = EntityVersionContext.lookup(entity);
		final XGettingTable<Integer, Entity> versions = context.versions(entity);
		versions.iterate(v ->
			System.out.println("Version " + v.key() + " = " + v.value())
		);
	}
	
}
