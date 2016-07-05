/**
 *
 */
package net.jadoth.experimental;

import net.jadoth.math.JadothMath;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestBresenham
{
	static String[][] initData()
	{
		return new String[][]{
			{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09"},
			{"10", "11", "12", "13", "14", "15", "16", "17", "18", "19"},
			{"20", "21", "22", "23", "24", "25", "26", "27", "28", "29"},
			{"30", "31", "32", "33", "34", "35", "36", "37", "38", "39"},
			{"40", "41", "42", "43", "44", "45", "46", "47", "48", "49"},
			{"50", "51", "52", "53", "54", "55", "56", "57", "58", "59"},
			{"60", "61", "62", "63", "64", "65", "66", "67", "68", "69"},
			{"70", "71", "72", "73", "74", "75", "76", "77", "78", "79"},
			{"80", "81", "82", "83", "84", "85", "86", "87", "88", "89"},
			{"90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}
		};
	}

	static final void print(final String[][] data)
	{
		for(final String[] strings : data)
		{
			for(final String string : strings)
			{
				System.out.print(string);
				System.out.print(' ');
			}
			System.out.println("");
		}
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
//		final String[][] data = initData();
//		final int[] line = intLinePoints(1, 1, 8, 2);
//		for(int i = 0; i < line.length; i+=2)
//		{
//			data[line[i]][line[i+1]] = "  ";
//		}
//
//		print(data);



//		intLine(1, 2, 98, 568, new IntCoordinateManipulator() {
//			@Override
//			public void manipulate(final int x, final int y)
//			{
//				System.out.println(x+","+y);
//			}
//		});

		int i = 0;
		long tStart, tStop;

//		System.out.println("2D:");
//		i = 10;
//		while(i-->0)
//		{
//			tStart = System.nanoTime();
//			JaMath.linePointsInt2D(1, 2, 89, 567);
//			tStop = System.nanoTime();
//			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}

		System.out.println("1D:");
		i = 10;
		while(i-->0)
		{
			tStart = System.nanoTime();
			JadothMath.linePointsInt1D(1, 2, 89, 567);
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}

//		System.out.println("Mani:");
//		i = 10;
//		while(i-->0)
//		{
//			tStart = System.nanoTime();
//			JaMath.line(1, 2, 89, 567, new IntCoordinateManipulator() {
//				@Override public void manipulate(final int x, final int y){}
//			});
//			tStop = System.nanoTime();
//			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}
//
//		System.out.println("Points:");
//		i = 10;
//		while(i-->0)
//		{
//			tStart = System.nanoTime();
//			JaMath.linePoints(1, 2, 89, 567);
//			tStop = System.nanoTime();
//			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//		}



		System.out.println(JadothMath.linePointsInt2D(1, 2, 89, 567).length);




	}

}
