package one.microstream.persistence.types;

public interface PersistenceTypeNameMapperLogging extends PersistenceTypeNameMapper, PersistenceLoggingWrapper<PersistenceTypeNameMapper>
{
	static PersistenceTypeNameMapperLogging New(final PersistenceTypeNameMapper wrapped)
	{
		return new Default(wrapped);
	}
	
	public class Default
		extends PersistenceLoggingWrapper.Abstract<PersistenceTypeNameMapper>
		implements PersistenceTypeNameMapperLogging
	{

		protected Default(final PersistenceTypeNameMapper wrapped)
		{
			super(wrapped);
		}

		@Override
		public String mapClassName(final String oldClassName)
		{
			final String mappedClassName = this.wrapped().mapClassName(oldClassName);
			
			if(mappedClassName != null)
			{
				this.logger().persistenceTypeNameMapper_afterMapClassName(oldClassName, mappedClassName);
			}
			
			return mappedClassName;
		}

		@Override
		public String mapInterfaceName(final String oldInterfaceName)
		{
			final String mappedInterfaceName = this.wrapped().mapInterfaceName(oldInterfaceName);
			
			if(mappedInterfaceName != null)
			{
				this.logger().persistenceTypeNameMapper_afterMapInterfaceName(oldInterfaceName, mappedInterfaceName);
			}
			
			return mappedInterfaceName;
		}
		
	}

}
