package one.microstream.configuration.types;

import one.microstream.chars.VarString;

/**
 * Assembler to export a configuration into an external format.
 *
 */
@FunctionalInterface
public interface ConfigurationAssembler
{
	/**
	 * Assembles all entries and child-configurations to an external format.
	 * 
	 * @param configuration the source
	 * @return a String representation of the external format
	 */
	public default VarString assemble(final Configuration configuration)
	{
		return this.assemble(VarString.New(), configuration);
	}
	
	/**
	 * Assembles all entries and child-configurations to an external format.
	 * 
	 * @param vs existing target VarString
	 * @param configuration the source
	 * @return a String representation of the external format
	 */
	public VarString assemble(VarString vs, Configuration configuration);
}
