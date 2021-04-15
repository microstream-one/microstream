package one.microstream.examples.layeredentities;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityVersionCleaner;
import one.microstream.entity.EntityVersionContext;
import one.microstream.examples.layeredentities._Address.AddressCreator;
import one.microstream.examples.layeredentities._Animal.AnimalCreator;
import one.microstream.examples.layeredentities._Human.HumanCreator;
import one.microstream.examples.layeredentities._Pet.PetCreator;


public final class EntityFactory
{
	final static JulLogger                     logger  = new JulLogger();
	final static EntityVersionCleaner<Integer> cleaner = EntityVersionCleaner.AmountPreserving(10);
	
	public static AddressCreator AddressCreator()
	{
		return addLayers(AddressCreator.New());
	}
	
	public static AnimalCreator AnimalCreator()
	{
		return addLayers(AnimalCreator.New());
	}
	
	public static HumanCreator HumanCreator()
	{
		return addLayers(HumanCreator.New());
	}
	
	public static PetCreator PetCreator()
	{
		return addLayers(PetCreator.New());
	}
	
	private static <E extends Entity, C extends Entity.Creator<E, C>> C addLayers(final C creator)
	{
		return creator
			.addLayer(logger)
			.addLayer(EntityVersionContext.AutoIncrementingInt(cleaner))
		;
	}
	
	private EntityFactory()
	{
	}
}
