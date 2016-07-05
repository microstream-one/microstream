package net.jadoth.meta;

import net.jadoth.util.chars.VarString;

public interface SettingValidator
{
	public VarString assemble(VarString vs, String fieldName, int fieldNameLength);
}
