package one.microstream.util.code;

public enum FieldType
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	MUTABLE                          (false, false, false, false),
	MUTABLE_WITH_CTOR                (false, true , false, false),
	MUTABLE_WITH_SETTER              (false, false, true , false),
	MUTABLE_WITH_SETTER_CTOR         (false, true , true , false),
	MUTABLE_WITH_SETTER_CHAINING     (false, false, true , true ),
	MUTABLE_WITH_SETTER_CHAINING_CTOR(false, true , true , true ),
	
	// final fields must always have a construtor paramter, except if there is an initializer.
	FINAL                            (true , true , false, false);
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private boolean isFinal;
	private boolean hasCtor;
	private boolean hasSetter;
	private boolean hasChainingSetter;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private FieldType(final boolean isFinal, final boolean hasCtor, final boolean hasSetter, final boolean hasChainingSetter)
	{
		this.isFinal = isFinal;
		this.hasCtor = hasCtor;
		this.hasSetter = hasSetter;
		this.hasChainingSetter = hasChainingSetter;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public boolean isFinal()
	{
		return this.isFinal;
	}
	
	public boolean hasCtor()
	{
		return this.hasCtor;
	}
	
	public boolean hasSetter()
	{
		return this.hasSetter;
	}
	
	public boolean hasChainingSetter()
	{
		return this.hasChainingSetter;
	}
	
	
}
