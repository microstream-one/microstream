package net.jadoth.experimental.properties;

import static net.jadoth.util.chars.VarString.New;

import net.jadoth.util.chars.VarString;

/**
 *
 * @author Thomas Muenz
 */
public interface SystemProperty extends GlobalProperty
{
	@Override
	public SystemProperty parent();

	public SystemProperty set(String value);


	public abstract class Hierarchy implements SystemProperty
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final SystemProperty parent;


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Hierarchy(final SystemProperty parent)
		{
			super();
			this.parent = parent;
		}

		@Override
		public SystemProperty parent()
		{
			return this.parent;
		}

	}


	public class Named extends Hierarchy
	{
		final String name;


		public Named(final SystemProperty parent, final String name)
		{
			super(parent);
			this.name = name;
		}

		public Named(final String name)
		{
			this(null, name);
		}



		@Override
		public String name()
		{
			return this.name;
		}

		@Override
		public String get()
		{
			return System.getProperty(this.key());
		}

		@Override
		public SystemProperty set(final String value)
		{
			System.setProperty(this.key(), value);
			return this;
		}

		@Override
		public VarString appendTo(final VarString vc)
		{
			final String value;
			if((value = this.get()) != null)
			{
				vc.add(value);
			}
			return vc;
		}

		protected String key()
		{
			return this.key(New()).toString();
		}

		protected VarString key(final VarString vc)
		{
			// assemble parent key component if present
			if(this.parent != null)
			{
				vc.add(this.parent.toString());
				if(this.name != null)
				{
					vc.append('.'); // tricky: separator only if both parent and name are present
				}
			}

			// assemble extension component if present
			if(this.name != null)
			{
				vc.add(this.name);
			}

			return vc; // return completed key string
		}

		@Override
		public String toString()
		{
			return this.key();
		}

	}

	public class Defaulted extends Named
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String defaultValue;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Defaulted(final SystemProperty parent, final String name, final String defaultValue)
		{
			super(parent, name);
			this.defaultValue = defaultValue;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String get()
		{
			return System.getProperty(this.key(), this.defaultValue);
		}

	}

	public class FallThrough extends Named
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		final String defaultValue;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public FallThrough(final SystemProperty parent, final String name, final String defaultValue)
		{
			super(parent, name);
			this.defaultValue = defaultValue;
		}




		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String get()
		{
			String value;
			if((value = System.getProperty(this.key())) != null)
			{
				return value;
			}

			for(SystemProperty pk = this.parent; pk != null; pk = pk.parent())
			{
				if((value = pk.get()) != null)
				{
					return value;
				}
			}

			return this.defaultValue;
		}

	}

	public abstract class Util
	{
		public static SystemProperty systemProperty(final String name)
		{
			return new SystemProperty.Named(null, name);
		}

		public static SystemProperty systemProperty(final SystemProperty parent, final String name)
		{
			return new SystemProperty.Named(parent, name);
		}

		public static SystemProperty systemPropertyDefaulted(final SystemProperty parent, final String name)
		{
			return new SystemProperty.Defaulted(parent, name, null);
		}

		public static SystemProperty systemPropertyDefaulted(final SystemProperty parent, final String name, final String defaultValue)
		{
			return new SystemProperty.Defaulted(parent, name, defaultValue);
		}

		public static SystemProperty systemPropertyDefaulted(final String name, final String defaultValue)
		{
			return new SystemProperty.Defaulted(null, name, defaultValue);
		}

		public static SystemProperty systemPropertyFallThrough(final SystemProperty parent, final String name)
		{
			return new SystemProperty.FallThrough(parent, name, null);
		}

		public static SystemProperty systemPropertyFallThrough(final SystemProperty parent, final String name, final String defaultValue)
		{
			return new SystemProperty.FallThrough(parent, name, defaultValue);
		}

		public static GlobalProperty globalPropertyComposite(final GlobalProperty parent, final GlobalProperty extension)
		{
			return new GlobalProperty.Composite(parent, extension, null);
		}

		public static GlobalProperty globalPropertyComposite(final SystemProperty... components)
		{
			if(components.length == 1)
			{
				return new GlobalProperty.Composite(components[0], null, null);
			}
			GlobalProperty current = components[0];
			for(int i = 1; i < components.length; i++)
			{
				current = new GlobalProperty.Composite(current, components[i], null);
			}
			return current;
		}

		public static GlobalProperty globalPropertyComposite(
			final GlobalProperty parent,
			final GlobalProperty extension,
			final String defaultExtensionValue
		)
		{
			return new GlobalProperty.Composite(parent, extension, defaultExtensionValue);
		}
	}

}
