package net.jadoth.test.reflection.copy;

public class Common {

	public static float staticCommonFloat = 3.14f;

	public int commonInt = 5;
	public String commonString = "Hallo";



	@Override
	public String toString() {
		final String n = "\n";
		return 	"commonInt = "+this.commonInt+n+
				"commonString = "+this.commonString+n;
	}




}
