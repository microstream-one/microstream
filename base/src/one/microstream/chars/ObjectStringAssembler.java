package one.microstream.chars;


@FunctionalInterface
public interface ObjectStringAssembler<T>
{
	public VarString assemble(VarString vs, T subject);
	
	public default VarString provideAssemblyBuffer()
	{
		// cannot make any assumptions about the required capacity in a generic implementation.
		return VarString.New();
	}
	
	public default String assemble(final T subject)
	{
		final VarString vs = this.provideAssemblyBuffer();
		
		this.assemble(vs, subject);
		
		return vs.toString();
	}
	
}
