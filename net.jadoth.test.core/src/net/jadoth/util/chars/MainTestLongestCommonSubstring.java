package net.jadoth.util.chars;

import java.util.Arrays;

import net.jadoth.chars.XChars;



public class MainTestLongestCommonSubstring
{
	static final String[][] strings = {
		{"aaabbb", "aaacc"},
		{"bbbaaa", "ccaaa"},
		{"bbbbbb", "ccccc"},
		{""      , ""     },
		{"a"     , "a"    },
		{"aaa"   , "aaa"  },
	};


	static void test(final String s1, final String s2, final char[] c1, final char[] c2)
	{
		System.out.println(XChars.longestCommonSubstring(s1, s2));
		System.out.println(XChars.longestCommonPrefix(s1, s2));
		System.out.println(XChars.longestCommonSuffix(s1, s2));

		System.out.println(Arrays.toString(XChars.longestCommonSubstring(c1, c2)));
		System.out.println(Arrays.toString(XChars.longestCommonPrefix(c1, c2)));
		System.out.println(Arrays.toString(XChars.longestCommonSuffix(c1, c2)));
	}



	public static void main(final String args[])
	{
		for(final String[] s : strings)
		{
			test(s[0], s[1], s[0].toCharArray(), s[1].toCharArray());
			System.out.println("--------------------");
		}
	}

}
