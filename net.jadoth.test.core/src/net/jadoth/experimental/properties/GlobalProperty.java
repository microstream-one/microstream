package net.jadoth.experimental.properties;

import net.jadoth.chars.VarString;

public interface GlobalProperty
{
	public GlobalProperty parent();
	
	public String name();

	public String get();
	public VarString appendTo(VarString vc);
	
	@Override
	public String toString();
	
	
	public class Reference extends Hierarchy
	{
		final Object referent;

		public Reference(final GlobalProperty parent, final Object referent)
		{
			super(parent);
			this.referent = referent;
		}

		@Override
		public String name()
		{
			return "<"+this.referent.getClass().getSimpleName()+">";
		}

		@Override
		public VarString appendTo(final VarString vc)
		{
			if(this.parent != null)
			{
				this.parent.appendTo(vc);
			}
			return vc.add(this.referent);
		}
		
	}
	
	
	
	public abstract class Hierarchy implements GlobalProperty
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final GlobalProperty parent;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Hierarchy(final GlobalProperty parent)
		{
			super();
			this.parent = parent;
		}
		
		@Override
		public GlobalProperty parent()
		{
			return this.parent;
		}
		
		@Override
		public String get()
		{
			// assemble value, but if no component was found/added whatsoever, return null to indicate not found.
			final VarString vc;
			return this.appendTo(vc = VarString.New()).length() == 0 ?null :vc.toString();
		}
		
	}
	
	
	public class Composite extends Hierarchy
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final GlobalProperty extension;
		final String defaultExtensionValue;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Composite(final GlobalProperty parent, final GlobalProperty extension, final String relativeDefault)
		{
			super(parent);
			this.extension = extension;
			this.defaultExtensionValue = relativeDefault;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////
		
		@Override
		public String name()
		{
			return this.extension == null ?null :this.extension.toString();
		}
		
		
		@Override
		public VarString appendTo(final VarString vc)
		{
			if(this.parent != null)
			{
				this.parent.appendTo(vc);
			}
			final int vcLength = vc.length();
			if(this.extension != null)
			{
				this.extension.appendTo(vc);
			}
			if(this.defaultExtensionValue != null && vcLength == vc.length())
			{
				vc.add(this.defaultExtensionValue);
			}
			return vc;
		}
		
		@Override
		public String toString()
		{
			final VarString vc = VarString.New();
			vc.append('(');
			if(this.parent != null)
			{
				vc.add(this.parent);
				if(this.extension != null)
				{
					vc.add(',', ' ');
				}
			}
			if(this.extension != null)
			{
				vc.add(this.extension);
			}
			return vc.append(')').toString();
		}
		
	}
}
