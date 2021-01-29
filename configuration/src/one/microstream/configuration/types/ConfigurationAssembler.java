package one.microstream.configuration.types;

import one.microstream.chars.VarString;

@FunctionalInterface
public interface ConfigurationAssembler
{
	public default VarString assemble(final Configuration configuration)
	{
		return this.assemble(VarString.New(), configuration);
	}
	
	public VarString assemble(VarString vs, Configuration configuration);
}
