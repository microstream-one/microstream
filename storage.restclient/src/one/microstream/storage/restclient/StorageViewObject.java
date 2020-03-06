
package one.microstream.storage.restclient;

import java.util.List;

import one.microstream.persistence.types.PersistenceTypeDescription;


public interface StorageViewObject extends StorageViewElement
{
	public long objectId();
	
	public PersistenceTypeDescription typeDescription();
	
	public String simpleTypeName();
	
	public String qualifiedTypeName();
	
	
	public static class Default extends StorageViewElement.Abstract implements StorageViewObject
	{
		private final long                       objectId;
		private final long                       fixedLength;
		private final PersistenceTypeDescription typeDescription;
		private List<StorageViewElement>         members;
		
		Default(
			final StorageView.Default view,
			final String name,
			final String value,
			final long objectId,
			final long fixedLength,
			final PersistenceTypeDescription typeDescription
		)
		{
			super(view, name, value);
			this.objectId        = objectId;
			this.fixedLength     = fixedLength;
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
		
		@Override
		public boolean hasMembers()
		{
			return this.typeDescription.allMembers().size() > 0;
		}
		
		@Override
		public List<StorageViewElement> members(
			final boolean forceRefresh
		)
		{
			if(this.members == null || forceRefresh)
			{
				this.members = this.view().members(this.objectId, this.fixedLength);
			}
			return this.members;
		}
		
		@Override
		public String toString()
		{
			return super.toString() + " [" + this.objectId + "]";
		}
		
	}
	
}
