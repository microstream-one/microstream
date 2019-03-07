package scalabashing;

// see http://newhoggy.wordpress.com/2011/01/11/declaring-variables-and-methods/
public class ScalaGrampf
{
	interface IntToInt
	{
		int call(int argument);
	}



	public static void main(final String[] args)
	{
		/*// scala example (7 lines). very confusing "Int goes to Int is some block where n goes to some algorithm"
		lazy val fib: Int => Int = { n =>
			if(n < 2)
			{
				return 1
			} else {
				return fib(n - 1) + fib(n - 2)
			}
		}
		*/

		// biased nonsense Scala-frenetic Java negative-example
//		final IntToInt[] fib = new IntToInt[1];
//		fib[0] = new IntToInt() {
//			public int call(final int n) {
//				if(n < 2)
//				{
//					return 1;
//				} else {
//					return fib[0].call(n - 1) + fib[0].call(n - 2);
//				}
//			}
//		};

		// proper unbiased Java syntax (7 lines). Granted with missing closure syntax
//		final IntToInt fib = new IntToInt(){ public int call(final int n) {
//			if(n < 2)
//			{
//				return 1;
//			} else {
//				return this.call(n - 1) + this.call(n - 2);
//			}
//		}};






		// Java 8 Syntax (and the winner in readability and writeability is: Java 8, by far)
		final IntToInt fib = ScalaGrampf::fib;

		/*// and if scala-like put-everything-in-one-functional-line is wished:
		final Function_int_int fib = #(int n){ n < 2 ? 1 : this.call(n - 1) + this.call(n - 2) };
		*/

		System.out.println(fib.call(5));

	}

	static int fib(final int n)
	{
		return n < 2 ?1 :fib(n - 1) + fib(n - 2);
	}
}
