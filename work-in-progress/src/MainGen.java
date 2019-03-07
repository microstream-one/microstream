import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.math.XMath;


public class MainGen
{
	static final char[]
		L1 = {'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'w'},
		L2 = {'a','e','i','o'}                                                                    ,
		L3 = {'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'w'},
		L5 = {'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'w'}
	;


	public static void main(final String[] args)
	{
		final XGettingSequence<String> generated = generate();
		generated.iterate(System.out::println);
		System.out.println("Amount: " + generated.size());
	}

	static XGettingSequence<String> generate()
	{
		return generate(100_000);
	}

	static XGettingSequence<String> generate(final int amount)
	{
		return generate(amount, 1_000_000);
	}

	static XGettingSequence<String> generate(final int amount, final int maxTries)
	{
		int i = 0, t = 0;

		final EqHashEnum<String> generated = EqHashEnum.New();
		while(i < amount && t < maxTries)
		{
			t++;
			if(generated.add(generateName()))
			{
				i++;
			}
		}

		generated.sort(String::compareTo);

		return generated;
	}


	static String generateName()
	{
		final char c1 = randomChar(L1);
		final char v  = randomChar(L2);
		final char c3 = randomChar(L3, c1);
		final char c5 = randomChar(L5, c1);

		return new String(new char[]{Character.toUpperCase(c1), v, c3, v, c5});
	}


	static char randomChar(final char[] source)
	{
		return source[XMath.random(source.length)];
	}

	static char randomChar(final char[] source, final char exclude1)
	{
		while(true)
		{
			final char c = randomChar(source);
			if(c != exclude1)
			{
				return c;
			}
		}
	}

}
