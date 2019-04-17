package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.math.XMath;

public interface BinaryField
{
	// (17.04.2019 TM)FIXME: MS-130: must have a connection to or even itself be a PersistenceTypeDefinitionMember.
	
	public Class<?> type();
	
	public String name();
	
	public long offset();
	
	
	
	public interface Initializable extends BinaryField
	{
		public long initializeOffset(long offset);
		
		public String initializeName(String name);
	}
	
	public final class Default implements BinaryField.Initializable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<?> type  ;
		private       String   name  ;
		private       long     offset = -1;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final Class<?> type)
		{
			super();
			this.type = type;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long initializeOffset(final long offset)
		{
			if(this.offset >= 0)
			{
				if(this.offset == offset)
				{
					return offset;
				}
				
				throw new RuntimeException(); // (04.04.2019 TM)EXCP: proper exception
			}
			
			return this.offset = XMath.notNegative(offset);
		}
		
		@Override
		public final String initializeName(final String name)
		{
			if(this.name != null)
			{
				if(this.name.equals(name))
				{
					return this.name;
				}
				
				throw new RuntimeException(); // (04.04.2019 TM)EXCP: proper exception
			}
			
			return this.name = notNull(name);
		}
		
		@Override
		public final Class<?> type()
		{
			return this.type;
		}
		
		@Override
		public final String name()
		{
			return this.name;
		}
		
		@Override
		public final long offset()
		{
			return XMath.notNegative(this.offset);
		}
		
	}
	
}
