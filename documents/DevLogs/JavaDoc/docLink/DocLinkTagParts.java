package doclink;

public interface DocLinkTagParts
{
	public String rawString();
	
	public String typeName();
	
	public String memberName();
	
	public String[] parameterList();
	
	public String tagName();
	
	public String extraIdentifier();
	
	public default boolean isMember()
	{
		return this.memberName() != null;
	}
	
	public default boolean isMethod()
	{
		// list can be non-null and empty
		return this.parameterList() != null;
	}
	
	
	
	public static final class Default implements DocLinkTagParts
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		String
			rawString      ,
			typeName       ,
			memberName     ,
			parameterList[],
			tagName        ,
			extraIdentifier
		;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String rawString()
		{
			return this.rawString;
		}
		
		@Override
		public final String typeName()
		{
			return this.typeName;
		}
		
		@Override
		public final String memberName()
		{
			return this.memberName;
		}
		
		@Override
		public final String[] parameterList()
		{
			return this.parameterList;
		}
		
		@Override
		public final String tagName()
		{
			return this.tagName;
		}
		
		@Override
		public final String extraIdentifier()
		{
			return this.extraIdentifier;
		}
		
	}
	
}
