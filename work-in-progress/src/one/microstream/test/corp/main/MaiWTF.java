package one.microstream.test.corp.main;

public class MaiWTF
{
	
	public static void main(final String[] args)
	{
		System.getProperties().forEach((k, v) -> System.out.println(k + "\t" + v));
		System.out.println(System.getProperty("java.version"));
	}
	
}
