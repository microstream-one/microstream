
package one.microstream.storage.restclient;

import java.util.Collections;
import java.util.List;

import one.microstream.persistence.types.PersistenceTypeDescription;


public interface StorageViewObject extends StorageViewElement
{
	public long objectId();
	
	public PersistenceTypeDescription typeDescription();
	
	public String simpleTypeName();
	
	public String qualifiedTypeName();
	
	
	public static abstract class Abstract extends StorageViewElement.Abstract implements StorageViewObject
	{
		final long                       objectId;
		final PersistenceTypeDescription typeDescription;
		
		Abstract(
			final StorageView.Default view,
			final String name,
			final String value,
			final long objectId,
			final PersistenceTypeDescription typeDescription
		)
		{
			super(view, name, value);
			
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
			final String qualifiedTypeName = this.qualifiedTypeName();
			final int i = qualifiedTypeName.lastIndexOf('.');
			return i == -1
				? qualifiedTypeName
				: qualifiedTypeName.substring(i + 1);
		}

		@Override
		public String qualifiedTypeName()
		{
			final String typeName = this.typeDescription.typeName();
			return typeName.startsWith("[")
				? qualifiedName(typeName)
				: typeName;
		}
		
		private static String qualifiedName(final String binaryName)
		{
			switch(binaryName.charAt(0))
			{
				case '[': return qualifiedName(binaryName.substring(1)).concat("[]");
				case 'L': return binaryName.substring(1, binaryName.length() - 1);
				case 'B': return "byte";
				case 'C': return "char";
				case 'D': return "double";
				case 'F': return "float";
				case 'I': return "int";
				case 'J': return "long";
				case 'S': return "short";
				case 'Z': return "boolean";
				default:
					return binaryName;
			}
		}
		
		@Override
		public String toString()
		{
			return super.toString() + " (" + this.simpleTypeName() + ") " + this.objectId;
		}
	}
	
	
	public static class Complex extends Abstract
	{
		private List<StorageViewElement> members;
		
		Complex(
			final StorageView.Default view,
			final String name,
			final String data,
			final long objectId,
			final PersistenceTypeDescription typeDescription
		)
		{
			super(view, name, data, objectId, typeDescription);
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
				this.members = this.view().members(this.objectId);
			}
			return this.members;
		}
		
	}
	
	
	public static class Simple extends Abstract
	{
		Simple(
			final StorageView.Default view,
			final String name,
			final String value,
			final long objectId,
			final PersistenceTypeDescription typeDescription
		)
		{
			super(view, name, value, objectId, typeDescription);
		}
		
		@Override
		public boolean hasMembers()
		{
			return false;
		}
		
		@Override
		public List<StorageViewElement> members(
			final boolean forceRefresh
		)
		{
			return Collections.emptyList();
		}
		
	}
	
}
