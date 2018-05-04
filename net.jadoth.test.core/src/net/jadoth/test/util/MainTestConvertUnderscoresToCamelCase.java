package net.jadoth.test.util;

import net.jadoth.chars.JadothChars;

public class MainTestConvertUnderscoresToCamelCase
{
	public static void main(final String[] args)
	{
		final String[] strings = {
			"_",
			"__",
			"tab_sum",
			"tab_sum_",
			"tab_Sum",
			"tab_Sum",
			"tab__sum",
			"tab___sum",
			"tab_9sum",
			"tab_�sum",
			"tab_�sum",
			"__tab_sum",
			"tab_sum__",
			"_tab_sum_",
			"TAB_SUM",
			"abc1_23ef",
			"abc�_abc",
			"abc�_Abc",
			"abc9_Abc",
			"abc9_abc",
			"abc�_�bc",
		};
		for(final String s : strings)
		{
			System.out.println(s+"\t->\t"+JadothChars.convertUnderscoresToCamelCase(s));
		}
	}
}
