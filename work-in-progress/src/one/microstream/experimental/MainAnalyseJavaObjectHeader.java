package one.microstream.experimental;

/**
 * Research experiment class that shows that the Java object header on 32 bit JVMs uses the full range of 25 bit
 * for the object's identity hash code, leaving 7 bits for status flags (and of course another 32 bits for the
 * object's type).<br>
 * The identity hash code value ranges from (1) to (2^25 - 8).
 * 
 * @author Thomas Muenz
 */
public class MainAnalyseJavaObjectHeader
{
	
	public static void main(final String[] args)
	{
		int minHash = Integer.MAX_VALUE;
		int maxHash = 0;
		int min2Bound = Integer.MAX_VALUE;
		int max2Bound = 0;
		
		while(true)
		{
			final Object o = new Object();
			
			final int hash = System.identityHashCode(o);
			if(hash < minHash) minHash = hash;
			if(hash > maxHash) maxHash = hash;
			
			final int bound = log2Bound(hash);
			if(bound < min2Bound) min2Bound = bound;
			if(bound > max2Bound) max2Bound = bound;
			
			System.out.println("("+min2Bound+","+max2Bound+"; "+minHash+","+maxHash+") "+bound+"\t"+hash);
			if(min2Bound == 0) System.exit(0);
		}
	}
	
	// copied from one.microstream.math.XMath.log2Bound
	static final int log2Bound(final int n)
	{
		int i = 1;
		int c = 0;
		while(i < n)
		{
			i <<= 1;
			c++;
		}
		return c;
	}
}
