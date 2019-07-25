package one.microstream.test;

import one.microstream.X;

public class MainTestTimes
{
	public static void main(final String[] args)
	{
		System.out.println("#1");
		X.times(5).iterate(i ->
			System.out.println(i)
		);

		System.out.println("#2");
		X.range(1, 5).iterate(i ->
			System.out.println(i)
		);

		System.out.println("#3");
		X.range(0, 4).iterate(i ->
			System.out.println(i)
		);

		System.out.println("#4");
		X.range(5, 10).iterate(i ->
			System.out.println(i)
		);

		System.out.println("#5");
		X.range(10, 5).iterate(i ->
			System.out.println(i)
		);

		System.out.println("#6");
		X.range(-5, 5).iterate(i ->
			System.out.println(i)
		);

		System.out.println("#7");
		X.range(5, -5).iterate(i ->
			System.out.println(i)
		);
	}
}
