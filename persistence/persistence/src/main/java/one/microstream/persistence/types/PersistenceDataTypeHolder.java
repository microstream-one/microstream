package one.microstream.persistence.types;

public interface PersistenceDataTypeHolder<D>
{
	public Class<D> dataType();
	
	
	public class Default<D> implements PersistenceDataTypeHolder<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<D> dataType;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(final Class<D> dataType)
		{
			super();
			this.dataType = dataType;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Class<D> dataType()
		{
			return this.dataType;
		}
		
	}
	
}
