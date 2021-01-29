package one.microstream.configuration.types;

import one.microstream.chars.VarString;

public interface ConfigurationAssemblerIni extends ConfigurationAssembler
{
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
