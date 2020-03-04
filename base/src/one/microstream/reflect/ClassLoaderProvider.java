package one.microstream.reflect;

@FunctionalInterface
public interface ClassLoaderProvider
{
	/**
	 * Provides the {@link ClassLoader} instance to be used with {@link XReflect#resolveType(String, ClassLoader)}
	 * to resolve the passed typeName.<br>
	 * The {@literal typeName} should usually not be required to determine the returned {@link ClassLoader}.
	 * It is just an optionally available information in case the responsible {@link ClassLoader} has to be determined
	 * based on the specific type (e.g. the package path or a sub path of it).
	 * 
	 * @param typeName the full qualified name of the type to be resolved.
	 * 
	 * @return the {@link ClassLoader} instance to be used to resolve the passed type name.
	 */
	public ClassLoader provideClassLoader(String typeName);
	
	
	
	/**
	 * The {@link ClassLoader} used by the default {@link ClassLoaderProvider} implementation.
	 * 
	 * @return {@code ClassLoader.getSystemClassLoader()}
	 */
	public static ClassLoader defaultClassLoader()
	{
		return ClassLoader.getSystemClassLoader();
	}
	
	
	
	public static ClassLoaderProvider New()
	{
		return new ClassLoaderProvider.Default();
	}
	
	public final class Default implements ClassLoaderProvider
	{
		Default()
		{
			super();
		}
		
		@Override
		public ClassLoader provideClassLoader(final String typeName)
		{
			return ClassLoaderProvider.defaultClassLoader();
		}
		
	}
}
