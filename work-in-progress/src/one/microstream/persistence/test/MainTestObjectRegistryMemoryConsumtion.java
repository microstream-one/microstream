package one.microstream.persistence.test;

import one.microstream.chars.XChars;
import one.microstream.memory.XMemory;


public class MainTestObjectRegistryMemoryConsumtion
{
	public static void main(final String[] args)
	{
//		XReflect.iterateAllClassFields(ObjectRegistryGrowingRange.Entry.class, f ->
//		{
//			if(!Modifier.isStatic(f.getModifiers()))
//			{
//				System.out.println(f.getName() +": "+ XVM.objectFieldOffset(f));
//			}
//		});
//		System.out.println(
//			ObjectRegistryGrowingRange.Entry.class.getName()
//			+ " instance byte size:"
//				+ XVM.byteSizeInstance(ObjectRegistryGrowingRange.Entry.class)
//		);
		
		System.out.println("^ anything above is ridiculous");
		System.out.println("H.den. B/entry Perf.");
		printMemoryFootprintPerEntry64bit(16.00, "~ 55%");
		printMemoryFootprintPerEntry64bit( 8.00, "~ 65%" );
		printMemoryFootprintPerEntry64bit( 4.00, "~ 70%");
		printMemoryFootprintPerEntry64bit( 3.20, "~ 75%");
		printMemoryFootprintPerEntry64bit( 2.00, "~ 80%");
		printMemoryFootprintPerEntry64bit( 1.60, "~ 85%");
		printMemoryFootprintPerEntry64bit( 1.00, "=100%" );
		printMemoryFootprintPerEntry64bit( 0.80, "~105%" );
		printMemoryFootprintPerEntry64bit( 0.50, "~110%" );
		System.out.println("v anything below is ridiculous");
	}
	
	static void printMemoryFootprintPerEntry64bit(final double hashDensity, final String note)
	{
		printMemoryFootprintPerEntry64bit(XMemory.byteSizeReference(), hashDensity, note);
	}
	
	static void printMemoryFootprintPerEntry64bit(final int refByteSize, final double hashDensity, final String note)
	{
		/* Explanation:
		 * - Entry instances occupy 64 bytes, including memory padding (16 bytes header, 4 references, 1 long, 1 int)
		 * - The hash density specifies the average length of the hash chain in a hash table slot.
		 * - Arrays have a header length of 24 (16 + 4 for the length plus padding for 8-byte-alignment)
		 * - All entries in one hash chain share the bucket array total length and the two hash table slots.
		 * 
		 * Conclusion:
		 * The major memory eater is the luxuriant JDK WeakReference implementation.
		 * Increasing the hash density up to 16.0 can be reasonable to reduce memory consumption.
		 * Blame the rest on the JDK.
		 */
		final double result = 8 + refByteSize + 4*refByteSize + 16d
			+ 2d * (8+2*hashDensity + hashDensity*refByteSize) / hashDensity
			+ 2d * refByteSize / hashDensity
		;
		System.out.println(
			(hashDensity < 10 ? " " : "")
			+ new java.text.DecimalFormat("0.00").format(hashDensity)
			+ ": "+ (result < 100 ? " " : "")
			+ new java.text.DecimalFormat("0.00").format(result)
			+ (XChars.isEmpty(note) ? "" : "  " + note)
		);
	}
}
