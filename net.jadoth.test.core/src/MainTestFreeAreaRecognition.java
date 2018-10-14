import net.jadoth.chars.VarString;



public class MainTestFreeAreaRecognition
{
	private static final int WEIGHT_FREE = 1;

	// values around -10 are good for recognizing spots but bad for circles
	private static final int WEIGHT_BLOCKED = -10;

//	static final int[][] map = {
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,1,1,0,1,0,0,1,0,0,0,1,0,0,0},
//		{0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0},
//		{0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0},
//		{0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,0},
//		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
//	};
//	static final int[][] map = {
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//	};
//	static final int[][] map = {
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0},
//		{0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0},
//		{0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0},
//		{1,0,0,1,0,0,1,1,0,1,1,0,0,0,0,0},
//		{0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0},
//		{0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0},
//		{0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0},
//		{0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0},
//		{0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0},
//		{0,0,0,1,0,0,1,1,0,1,1,0,0,1,0,0},
//		{0,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//	};
//	static final int[][] map = {
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0},
//		{0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0},
//		{0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
//		{1,0,0,1,0,0,1,0,0,0,1,0,0,0,0,0},
//		{0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0},
//		{0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0},
//		{0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0},
//		{0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0},
//		{0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0},
//		{0,0,0,1,0,0,1,0,0,0,1,0,0,1,0,0},
//		{0,0,1,0,0,0,0,1,1,1,0,0,1,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0},
//		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
//	};

	static final int[][] map = {
		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,1,1,0,1,0,0,1,0,0,0,0,0,0,0},
		{0,0,1,1,0,0,0,0,0,0,0,0,1,0,0,0},
		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0},
		{0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0},
		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
		{0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0},
	};





	/* also reasonable results:
	 * DIAG =  90, NEXT = 128, SELF = 128, DIV = 111 (111 is very strange ^^, = 0.867 * 128)
	 * not very exact: DIAG = 181, NEXT = 128, SELF = 128, DIV = 151 (151 is even stranger)
	 */

	private static final int DIAG = 128;
	private static final int NEXT = 128;
	private static final int SELF = 128;
	private static final int DIV  = 128;

	private static final int RANGE = 5;

	public static void main(final String[] args)
	{
		print("Input:", map);
		print("analysisA"+RANGE+":", analyzeFading(markFree(map), RANGE));
		print("analysisB"+RANGE+":", analyzeBound(markFree(map), RANGE));
	}




	static final int[][] analyzeFading(final int[][] src, final int range)
	{
		int[][] analysis = internalAnalyzeFading(src, deepclone(src), range);
		analysis = normalize100(analysis, range);
		return analysis;
	}

	static final int[][] analyzeBound(final int[][] src, final int range)
	{
		int[][] analysis = internalAnalyzeBound(src, deepclone(src), 1, range);
		analysis = normalize100(analysis, range);
		return analysis;
	}




	static final int[][] normalizeRng(final int[][] source, int range)
	{
		int factor = 1;
		for(int r = range; r --> 0; )
		{
			factor *= 9;
		}

		range = 2*range;
		for(int y = 0; y < source.length; y++)
		{
			for(int x = 0; x < source[y].length; x++)
			{
				source[y][x] = source[y][x]< 0 ?0 :source[y][x]*range/factor;
			}
		}
		return source;
	}

	static final int[][] normalize100(final int[][] source, final int range)
	{
		int factor = 1;
		for(int r = range; r --> 0; )
		{
			factor *= 9;
		}
		for(int y = 0; y < source.length; y++)
		{
			for(int x = 0; x < source[y].length; x++)
			{
				source[y][x] = source[y][x]< 0 ?0 :source[y][x]*100/factor;
			}
		}
		return source;
	}

	static final int[][] normalizeNon(final int[][] source, final int range)
	{
		for(int y = 0; y < source.length; y++)
		{
			for(int x = 0; x < source[y].length; x++)
			{
				source[y][x] = source[y][x]< 0 ?0 :source[y][x];
			}
		}
		return source;
	}







	static final int[][] deepclone(final int[][] original)
	{
		final int[][] clone = original.clone();
		for(int y = 0; y < clone.length; y++)
		{
			clone[y] = clone[y].clone();
		}
		return clone;
	}
	static final int[][] copy(final int[][] source, final int[][] target)
	{
		for(int y = 0; y < source.length; y++)
		{
			for(int x = 0; x < source[0].length; x++)
			{
				target[y][x] = source[y][x];
			}
		}
		return target;
	}
	static final void print(final String title, final int[][] original)
	{
		final VarString vc = VarString.New();
		if(title != null)
		{
			vc.add(title).lf();
		}
		for(int y = 0; y < original.length; y++)
		{
			for(int x = 0; x < original[y].length; x++)
			{
				vc.add(original[y][x]).append('\t');
			}
			vc.setLast('\n');
		}
		System.out.println(vc);
	}
	static final void print(final int[][] original)
	{
		print(null, original);
	}


	static final int[][] markFree(final int[][] source)
	{
		final int[][] target = deepclone(source);
		for(int y = 0; y < source.length; y++)
		{
			for(int x = 0; x < source[y].length; x++)
			{
				target[y][x] = source[y][x] == 0 ?WEIGHT_FREE :WEIGHT_BLOCKED;
			}
		}
		return target;
	}







	static final int[][] internalAnalyzeBound(final int[][] src, final int[][] target, final int r, final int range)
	{
		final int eY = src.length-r;
		final int eX = src[0].length-r;

		for(int y = r; y < eY; y++)
		{
			for(int x = r; x < eX; x++)
			{
				if(src[y][x] <= 0) continue;
				target[y][x] = (
					(src[y-1][x-1]*DIAG) + (src[y-1][x  ]*NEXT) + (src[y-1][x+1]*DIAG) +
					(src[y  ][x-1]*NEXT) + (src[y  ][x  ]*SELF) + (src[y  ][x+1]*NEXT) +
					(src[y+1][x-1]*DIAG) + (src[y+1][x  ]*NEXT) + (src[y+1][x+1]*DIAG)
				)/DIV;
			}
		}
//		print("current(range="+r+")",target); // (23.04.2011)XXX: debug
		return r == range ?target : internalAnalyzeBound(target, copy(target, src), r+1, range);
	}

	static final int[][] internalAnalyzeFading(final int[][] src, final int[][] target, final int range)
	{
		final int eY = src.length-1;
		final int eX = src[0].length-1;

		// (23.04.2011)TODO actually, range would have to determine border offset as well

		// 1. inner area
		for(int y = 1; y < eY; y++)
		{
			for(int x = 1; x < eX; x++)
			{
				if(src[y][x] <= 0) continue;
				target[y][x] = (
					(src[y-1][x-1]*DIAG) + (src[y-1][x  ]*NEXT) + (src[y-1][x+1]*DIAG) +
					(src[y  ][x-1]*NEXT) + (src[y  ][x  ]*SELF) + (src[y  ][x+1]*NEXT) +
					(src[y+1][x-1]*DIAG) + (src[y+1][x  ]*NEXT) + (src[y+1][x+1]*DIAG)
				)/DIV;
			}
		}

		// 2. edges
		for(int x = 1; x < eX; x++)
		{
			target[0][x] = (
				(src[0][x-1]*NEXT) + (src[0][x]*SELF) + (src[0][x+1]*NEXT) +
				(src[1][x-1]*DIAG) + (src[1][x]*NEXT) + (src[1][x+1]*DIAG)
			)/DIV;
			target[eY][x] = (
				(src[eY-1][x-1]*DIAG) + (src[eY-1][x]*NEXT) + (src[eY-1][x+1]*DIAG) +
				(src[eY  ][x-1]*NEXT) + (src[eY  ][x]*SELF) + (src[eY  ][x+1]*NEXT)
			)/DIV;
		}
		for(int y = 1; y < eY; y++)
		{
			target[y][0] = (
				(src[y-1][0]*NEXT) + (src[y-1][1]*DIAG) +
				(src[y  ][0]*SELF) + (src[y  ][1]*NEXT) +
				(src[y+1][0]*NEXT) + (src[y+1][1]*DIAG)
			)/DIV;
			target[y][eX] = (
				(src[y-1][eX-1]*DIAG) + (src[y-1][eX]*NEXT) +
				(src[y  ][eX-1]*NEXT) + (src[y  ][eX]*SELF) +
				(src[y+1][eX-1]*DIAG) + (src[y+1][eX]*NEXT)
			)/DIV;
		}

		// 3. corners
		target[0 ][0 ] = ((src[0 ][0 ]*SELF) + (src[0 ][1   ]*NEXT) + (src[1   ][0 ]*NEXT) + (src[1   ][1   ]*DIAG))/DIV;
		target[0 ][eX] = ((src[0 ][eX]*SELF) + (src[0 ][eX-1]*NEXT) + (src[1   ][eX]*NEXT) + (src[1   ][eX-1]*DIAG))/DIV;
		target[eY][0 ] = ((src[eY][0 ]*SELF) + (src[eY][1   ]*NEXT) + (src[eY-1][0 ]*NEXT) + (src[eY-1][1   ]*DIAG))/DIV;
		target[eY][eX] = ((src[eY][eX]*SELF) + (src[eY][eX-1]*NEXT) + (src[eY-1][eX]*NEXT) + (src[eY-1][eX-1]*DIAG))/DIV;

		return range == 1 ?target : internalAnalyzeFading(target, src, range-1);
	}



//	static final int[][] internalAnalyze(final int[][] src, final int[][] target, final int range)
//	{
//		final int eY = src.length-1;
//		final int eX = src[0].length-1;
//
//		// 1. inner area
//		for(int y = 1; y < eY; y++)
//		{
//			for(int x = 1; x < eX; x++)
//			{
//				if(src[y][x] <= 0) continue;
//				target[y][x] =
//					src[y-1][x-1] + src[y-1][x  ] + src[y-1][x+1] +
//					src[y  ][x-1] + src[y  ][x  ] + src[y  ][x+1] +
//					src[y+1][x-1] + src[y+1][x  ] + src[y+1][x+1]
//				;
//			}
//		}
//
//		// 2. edges
//		for(int x = 1; x < eX; x++)
//		{
//			target[0][x] =
//				src[0][x-1] + src[0][x] + src[0][x+1] +
//				src[1][x-1] + src[1][x] + src[1][x+1]
//			;
//			target[eY][x] =
//				src[eY-1][x-1] + src[eY-1][x] + src[eY-1][x+1] +
//				src[eY  ][x-1] + src[eY  ][x] + src[eY  ][x+1]
//			;
//		}
//		for(int y = 1; y < eY; y++)
//		{
//			target[y][0] =
//				src[y-1][0] + src[y-1][1] +
//				src[y  ][0] + src[y  ][1] +
//				src[y+1][0] + src[y+1][1]
//			;
//			target[y][eX] =
//				src[y-1][eX-1] + src[y-1][eX] +
//				src[y  ][eX-1] + src[y  ][eX] +
//				src[y+1][eX-1] + src[y+1][eX]
//			;
//		}
//
//		// 3. corners
//		target[0 ][0 ] = src[0 ][0 ] + src[0 ][1   ] + src[1   ][0 ] + src[1   ][1   ];
//		target[0 ][eX] = src[0 ][eX] + src[0 ][eX-1] + src[1   ][eX] + src[1   ][eX-1];
//		target[eY][0 ] = src[eY][0 ] + src[eY][1   ] + src[eY-1][0 ] + src[eY-1][1   ];
//		target[eY][eX] = src[eY][eX] + src[eY][eX-1] + src[eY-1][eX] + src[eY-1][eX-1];
//
//		return range == 1 ?target : internalAnalyze(target, src, range-1);
//	}
//
//	static final int[][] internalAnalyzeWeighted_s7m181(final int[][] src, final int[][] target, final int range)
//	{
//		final int eY = src.length-1;
//		final int eX = src[0].length-1;
//
//		// 1. inner area
//		for(int y = 1; y < eY; y++)
//		{
//			for(int x = 1; x < eX; x++)
//			{
//				if(src[y][x] <= 0) continue;
//				target[y][x] = (
//					(src[y-1][x-1]*181) + (src[y-1][x  ]<< 7) + (src[y-1][x+1]*181) +
//					(src[y  ][x-1]<< 7) + (src[y  ][x  ]<< 7) + (src[y  ][x+1]<< 7) +
//					(src[y+1][x-1]*181) + (src[y+1][x  ]<< 7) + (src[y+1][x+1]*181)
//				)>>>7;
//			}
//		}
//
//		// 2. edges
//		for(int x = 1; x < eX; x++)
//		{
//			target[0][x] = (
//				(src[0][x-1]<< 7) + (src[0][x]<< 7) + (src[0][x+1]<< 7) +
//				(src[1][x-1]*181) + (src[1][x]<< 7) + (src[1][x+1]*181)
//			)>>>7;
//			target[eY][x] = (
//				(src[eY-1][x-1]*181) + (src[eY-1][x]<< 7) + (src[eY-1][x+1]*181) +
//				(src[eY  ][x-1]<< 7) + (src[eY  ][x]<< 7) + (src[eY  ][x+1]<< 7)
//			)>>>7;
//		}
//		for(int y = 1; y < eY; y++)
//		{
//			target[y][0] = (
//				(src[y-1][0]<< 7) + (src[y-1][1]*181) +
//				(src[y  ][0]<< 7) + (src[y  ][1]<< 7) +
//				(src[y+1][0]<< 7) + (src[y+1][1]*181)
//			)>>>7;
//			target[y][eX] = (
//				(src[y-1][eX-1]*181) + (src[y-1][eX]<< 7) +
//				(src[y  ][eX-1]<< 7) + (src[y  ][eX]<< 7) +
//				(src[y+1][eX-1]*181) + (src[y+1][eX]<< 7)
//			)>>>7;
//		}
//
//		// 3. corners
//		target[0 ][0 ] = ((src[0 ][0 ]<< 7) + (src[0 ][1   ]<< 7) + (src[1   ][0 ]<< 7) + (src[1   ][1   ]*181))>>>7;
//		target[0 ][eX] = ((src[0 ][eX]<< 7) + (src[0 ][eX-1]<< 7) + (src[1   ][eX]<< 7) + (src[1   ][eX-1]*181))>>>7;
//		target[eY][0 ] = ((src[eY][0 ]<< 7) + (src[eY][1   ]<< 7) + (src[eY-1][0 ]<< 7) + (src[eY-1][1   ]*181))>>>7;
//		target[eY][eX] = ((src[eY][eX]<< 7) + (src[eY][eX-1]<< 7) + (src[eY-1][eX]<< 7) + (src[eY-1][eX-1]*181))>>>7;
//
//		return range == 1 ?target : internalAnalyze(target, src, range-1);
//	}


}


/* (23.04.2011)NOTE: Idea to recognize obstacle in range:
 * a)
 * while summing up neighbor values, if one neighbor is value 0, set current field value to 1 and continue.
 *
 * b)
 * in first step (where all fields are only "1"s) multiply current field value with all values
 * around it. If one of them is 0, the current field itself gets 0.
 * In any case, add 1 at the end (results in 2 for "free" fields, 1 for next-to-blocked fields).
 * Then proceed with usual summing up neighbor values.
 *
 */