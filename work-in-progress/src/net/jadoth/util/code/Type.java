package net.jadoth.util.code;

import net.jadoth.chars.VarString;

public interface Type
{
	public VarString assembleTypeName(VarString vs);

	public VarString assembleImplementationFullName(VarString vs);

	public VarString assembleImplementationClassName(VarString vs);
	
	

	public final class Implementation implements Type
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String typeName;
		private final String className;

		private final String fullName;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final String typeName, final String className)
		{
			super();
			this.typeName = typeName;
			this.className = className;
			this.fullName = typeName + '.' + className;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final VarString assembleTypeName(final VarString vs)
		{
			return vs.add(this.typeName);
		}

		@Override
		public final VarString assembleImplementationClassName(final VarString vs)
		{
			return vs.add(this.className);
		}

		@Override
		public final VarString assembleImplementationFullName(final VarString vs)
		{
			return vs.add(this.fullName);
		}
		
	}

}
