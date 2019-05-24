package doclink;

public interface DocLinkerYielding extends DocLinker
{
	@Override
	public default DocLinkerYielding processDoc(final String doc)
	{
		DocLinker.super.processDoc(doc);
		return this;
	}
	
	@Override
	public default DocLinkerYielding processDoc(final String doc, final String qualifiedTypeName)
	{
		DocLinker.super.processDoc(doc, qualifiedTypeName);
		return this;
	}
	
	@Override
	public DocLinkerYielding processDoc(String doc, String qualifiedTypeName, String parameterName);
	
	public DocLinkerYielding prepare();
	
	public String yield();
	
	
	public abstract class Abstract extends DocLinker.Abstract implements DocLinkerYielding
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract()
		{
			super();
		}

		protected Abstract(final CharsBuilder charsBuilder)
		{
			super(charsBuilder);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public DocLinkerYielding prepare()
		{
			this.internalPrepare();
			return this;
		}
		
		@Override
		public String yield()
		{
			return this.internalYield();
		}
		
		@Override
		public DocLinkerYielding.Abstract processDoc(
			final String doc              ,
			final String qualifiedTypeName,
			final String parameterName
		)
		{
			super.processDoc(doc, qualifiedTypeName, parameterName);
			return this;
		}
		
	}
}
