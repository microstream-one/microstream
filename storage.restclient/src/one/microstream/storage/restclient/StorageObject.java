
package one.microstream.storage.restclient;

import java.util.List;
import java.util.function.Function;

import one.microstream.persistence.types.PersistenceTypeDescription;


public interface StorageObject extends StorageViewElement
{
	public long objectId();
	
	public PersistenceTypeDescription typeDescription();
	
	public String simpleTypeName();
	
	public String qualifiedTypeName();
	
	
		
	
	public static class Default extends StorageViewElement.Default implements StorageObject
	{
		private final long                                         objectId;
		private final PersistenceTypeDescription                   typeDescription;
		
		Default(
			final StorageViewElement parent,
			final String name,
			final String value,
			final long objectId,
			final PersistenceTypeDescription typeDescription,
			final Function<StorageViewElement, List<StorageViewElement>> membersSupplier
		)
		{
			super(parent, name, value, membersSupplier);
			this.objectId        = objectId;
			this.typeDescription = typeDescription;
		}
		
		Default(
			final StorageViewElement parent,
			final String name,
			final String value,
			final long objectId,
			final PersistenceTypeDescription typeDescription,
			final Function<StorageViewElement, List<StorageViewElement>> membersSupplier,
			final List<StorageViewElement> members
		)
		{
			super(parent, name, value, membersSupplier, members);
			this.objectId        = objectId;
			this.typeDescription = typeDescription;
		}

		@Override
		public long objectId()
		{
			return this.objectId;
		}
		
		@Override
		public PersistenceTypeDescription typeDescription()
		{
			return this.typeDescription;
		}

		@Override
		public String simpleTypeName()
		{
			// TODO see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
			return this.typeDescription.typeName();
		}

		@Override
		public String qualifiedTypeName()
		{
			// TODO see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
			return this.typeDescription.typeName();
		}
		
	}
	
}
