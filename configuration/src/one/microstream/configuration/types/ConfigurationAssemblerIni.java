package one.microstream.configuration.types;

import one.microstream.chars.VarString;

/**
 * Assembler for configurations to export to INI (properties) format.
 * 
 * @see Configuration#store(ConfigurationStorer, ConfigurationAssembler)
 */
public interface ConfigurationAssemblerIni extends ConfigurationAssembler
{
	/**
	 * Pseudo-constructor to create a new INI assembler.
	 * 
	 * @return a new INI assembler
	 */
	public static ConfigurationAssemblerIni New()
	{
		return new ConfigurationAssemblerIni.Default();
	}
	
	
	public static class Default implements ConfigurationAssemblerIni
	{
		Default()
		{
			super();
		}
		
		@Override
		public VarString assemble(
			final VarString     vs           ,
			final Configuration configuration
		)
		{
			configuration.coalescedTable().iterate(kv ->
				vs.add(kv.key()).add(" = ").add(kv.value()).lf()
			);
			
			return vs;
		}
		
	}
	
}
