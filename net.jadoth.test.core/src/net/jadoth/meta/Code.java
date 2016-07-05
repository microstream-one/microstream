package net.jadoth.meta;

import net.jadoth.util.chars.VarString;

public final class Code
{
	// CHECKSTYLE.OFF: ConstantName: keyword names are intentionally unchanged

	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final String VISIBILITY_private   = "private";
	private static final String VISIBILITY_protected = "protected";
	private static final String VISIBILITY_public    = "public";

	static final String toLowerCaseFirstLetter(final String s)
	{
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	static final String toUpperCaseFirstLetter(final String s)
	{
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	static final VarString appendOverride(final VarString vs, final int level)
	{
		return vs.lf().repeat(level, '\t').add("@Override");
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////


	static final VarString sectionHeader(final VarString vs, final int level, final String title)
	{
		return vs
		.lf().tab(level).repeat(76, '/') // 76 plus 4 for reasonable tab size yields 80 width
		.lf().tab(level).repeat(2, '/').blank().add(title).blank().repeat(2, '/')
		.lf().tab(level).repeat(6 + title.length() - 1, '/')
		;
	}


	public static final Visibility private$ = new Visibility()
	{
		@Override
		public VarString assemble(final VarString vs)
		{
			return vs.add(VISIBILITY_private).blank();
		}
	};

	public static final Visibility DEFAULT = new Visibility()
	{
		@Override
		public VarString assemble(final VarString vs)
		{
			return vs;
		}
	};

	public static final Visibility protected$ = new Visibility()
	{
		@Override
		public VarString assemble(final VarString vs)
		{
			return vs.add(VISIBILITY_protected).blank();
		}
	};

	public static final Visibility public$ = new Visibility()
	{
		@Override
		public VarString assemble(final VarString vs)
		{
			return vs.add(VISIBILITY_public).blank();
		}
	};


	private Code()
	{
		// static only
		throw new UnsupportedOperationException();
	}

	// CHECKSTYLE.ON: ConstantName
}
