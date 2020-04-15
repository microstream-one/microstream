package one.microstream.entity;

import java.nio.file.Paths;
import java.util.logging.Logger;

import one.microstream.entity._Address.AddressCreator;
import one.microstream.entity._Customer.CustomerCreator;
import one.microstream.entity._Customer.CustomerUpdater;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class MainTestLayeredEntities
{
	public static void main(String[] args)
	{		
		EmbeddedStorageManager storage = EmbeddedStorage.start(Paths.get("storage-entities"));
		if(storage.root() == null)
		{
			JulLogger logger = new JulLogger();
			EntityVersionContext<Long> versionContext = EntityVersionContext.AutoIncrementingLong(
//				EntityVersionCleaner.AmountPreserving(2)
			);
			
			Customer customer = CustomerCreator.New()
				.addLayer(logger)
				.addLayer(versionContext)
				.firstName("Hans")
				.lastName("Meier")
				.address(AddressCreator.New().city("Weiden").create())
				.create();
			CustomerUpdater.setFirstName(customer, "B1");
			CustomerUpdater.setFirstName(customer, "B2");
			CustomerUpdater.setFirstName(customer, "B3");			
			storage.setRoot(customer);
			storage.storeRoot();
		}
		
		Customer customer = (Customer)storage.root();
//		CustomerUpdater.setFirstName(customer, "B4");
//		storage.storeRoot();
		System.out.println(customer);
		storage.shutdown();
	}
	
	
	static class JulLogger implements EntityLogger
	{
		@Override
		public void afterUpdate(
			final Entity identity, 
			final Entity data, 
			final boolean successful)
		{
			Logger.getLogger(identity.getClass().getName())
				.info("Entity updated");
		}
	}
}
