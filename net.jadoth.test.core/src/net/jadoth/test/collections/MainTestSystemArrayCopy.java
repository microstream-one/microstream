package net.jadoth.test.collections;

import net.jadoth.math.JadothMath;

public class MainTestSystemArrayCopy
{
	private static final int SIZE = 1000*1000;


	public static void main(final String[] args)
	{
		final int[] src = JadothMath.sequence(1,SIZE);
		final int[] dest = new int[SIZE];
		final int range = 2;
		final int length = SIZE-range;

//		System.out.println(Arrays.toString(dest).substring(0, 100)+"...");

		long tStart, tStop;
		for(int k = 50; k --> 0;)
		{
			tStart = System.nanoTime();
			for(int i = 0; i < length; i++)
			{
//				System.arraycopy(src, i, dest, i, range);
				arraycopy(src, i, dest, i, range);
//				dest[i] = src[i];
			}
			tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
		}





//		System.out.println(Arrays.toString(dest).substring(0, 100)+"...");

	}

	private static void arraycopy(final int[] src, final int srcPos, final int[] dest, final int destPos, final int length)
	{
		switch(length){
//			case 6:
//				dest[destPos+5] = src[srcPos+5];
			case 5:
				dest[destPos+4] = src[srcPos+4];
			case 4:
				dest[destPos+3] = src[srcPos+3];
			case 3:
				dest[destPos+2] = src[srcPos+2];
			case 2:
				dest[destPos+1] = src[srcPos+1];
			case 1: {
				dest[destPos  ] = src[srcPos  ];
				break;
			}
			default: System.arraycopy(src, srcPos, dest, destPos, length);
		}
	}

	static void arraycopy2(final int[] src, final int srcPos, final int[] dest, final int destPos, final int length)
	{
		switch(length){
			case 1: {
				dest[destPos  ] = src[srcPos  ];
				break;
			}
			case 2: {
				dest[destPos  ] = src[srcPos  ];
				dest[destPos+1] = src[srcPos+1];
				break;
			}
			case 3: {
				dest[destPos  ] = src[srcPos  ];
				dest[destPos+1] = src[srcPos+1];
				dest[destPos+2] = src[srcPos+2];
				break;
			}
			case 4: {
				dest[destPos  ] = src[srcPos  ];
				dest[destPos+1] = src[srcPos+1];
				dest[destPos+2] = src[srcPos+2];
				dest[destPos+3] = src[srcPos+3];
				break;
			}
//			case 5: {
//				dest[destPos  ] = src[srcPos  ];
//				dest[destPos+1] = src[srcPos+1];
//				dest[destPos+2] = src[srcPos+2];
//				dest[destPos+3] = src[srcPos+3];
//				dest[destPos+4] = src[srcPos+4];
//				break;
//			}
			default: System.arraycopy(src, srcPos, dest, destPos, length);
		}
	}

}
