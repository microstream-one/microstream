package one.microstream.examples.layeredentities;

import one.microstream.entity.Entity;

public interface Human extends Beeing<Human>, Named, Entity
{
	public Address address();
}
