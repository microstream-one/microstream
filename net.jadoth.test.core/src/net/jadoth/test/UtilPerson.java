package net.jadoth.test;

import java.util.Date;

public class UtilPerson
{
	// only male. No sexism. Just simplicity / laziness.
	private static final String[] FIRST_NAMES = {
		"James","John","Robert", "Michael","William","David", "Richard","Charles","Joseph", "Thomas","Christopher",
		"Daniel","Paul","Mark", "Donald","George","Kenneth", "Steven","Edward","Brian", "Ronald","Anthony","Kevin",
		"Jason","Matthew", "Gary","Timothy", "Jose","Larry", "Jeffrey","Frank", "Scott","Eric", "Stephen","Andrew",
		"Raymond","Gregory", "Joshua", "Jerry", "Dennis","Walter", "Patrick", "Peter", "Harold","Douglas", "Henry",
		"Carl","Arthur","Ryan","Roger","Joe","Jack","Albert","Jonathan","Justin","Terry","Gerald","Keith","Samuel",
		"Willie","Ralph","Lawrence", "Nicholas","Roy","Benjamin", "Bruce","Brandon","Adam", "Harry","Fred","Wayne",
		"Billy","Steve","Louis", "Jeremy","Randy","Howard", "Eugene","Carlos","Russell", "Bobby","Victor","Martin",
		"Phillip","Jesse", "Craig","Alan", "Clarence","Sean", "Philip","Chris", "Johnny","Earl", "Jimmy","Antonio",
		"Danny","Bryan", "Tony","Luis", "Mike","Stanley", "Leonard","Nathan", "Manuel","Rodney", "Curtis","Norman",
		"Allen","Marvin", "Vincent","Glenn", "Travis","Jeff", "Jacob","Lee", "Melvin", "Alfred", "Kyle", "Francis",
		"Bradley", "Jesus", "Herbert", "Frederick", "Ray", "Edwin", "Don", "Eddie", "Randall", "Barry","Alexander",
		"Bernard","Mario","Leroy", "Francisco","Marcus","Micheal", "Theodore","Oscar", "Jay", "Jim","Tom","Calvin",
		"Alex","Jon", "Bill","Lloyd", "Tommy","Leon", "Derek","Warren", "Darrell","Leo", "Tim", "Wesley", "Gordon",
		"Dean","Greg","Dustin", "Pedro","Derrick","Dan", "Corey","Maurice","Roberto", "Glen","Hector","Sam","Rick",
		"Brent","Ramon", "Charlie","Tyler", "Gilbert","Gene", "Marc","Reginald", "Ruben","Brett", "Angel", "Edgar",
		"Raul","Ben","Cecil","Andre","Brad","Gabriel","Ron","Mitchell","Roland","Arnold","Harvey","Jared","Adrian",
		"Karl", "Claude", "Erik", "Darryl", "Jamie", "Neil", "Christian"
	};

	private static final String[] LAST_NAMES = {
		"Smith","Johnson", "Williams","Jones", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor", "Anderson",
		"Thomas","Jackson","White","Harris","Martin","Thompson","Garcia","Robinson","Clark","Lewis","Lee","Walker",
		"Hall","Allen","Young","King","Wright","Hill","Scott","Green","Adams","Baker","Nelson","Carter","Mitchell",
		"Perez","Roberts","Turner","Phillips","Campbell", "Parker","Evans","Edwards","Collins","Stewart","Sanchez",
		"Morris","Rogers","Reed", "Cook","Morgan","Bell", "Murphy","Bailey","Rivera", "Cooper","Richardson", "Cox",
		"Howard","Ward","Torres","Peterson", "Gray","Ramirez","James","Watson", "Brooks","Kelly","Sanders","Price",
		"Bennett","Wood", "Barnes","Ross", "Henderson","Coleman", "Jenkins","Perry", "Powell", "Long", "Patterson",
		"Hughes","Flores", "Washington","Butler", "Simmons","Foster", "Gonzales", "Bryant", "Alexander", "Russell",
		"Griffin","Diaz", "Hayes","Myers", "Ford","Hamilton", "Graham","Sullivan", "Wallace","Woods","Cole","West",
		"Jordan","Owens", "Reynolds","Fisher", "Ellis","Harrison", "Gibson","Mcdonald", "Cruz","Marshall", "Ortiz",
		"Gomez","Banks","Freeman","Wells","Webb","Simpson","Stevens","Tucker","Porter","Hunter","Hicks","Crawford",
		"Henry","Boyd", "Mason","Morales", "Kennedy","Warren", "Dixon","Ramos", "Reyes", "Burns", "Gordon", "Shaw",
		"Holmes", "Rice", "Robertson", "Hunt", "Black" ,"Daniels", "Palmer", "Mills", "Nichols", "Grant", "Knight",
		"Ferguson","Rose", "Stone","Hawkins", "Dunn","Perkins", "Hudson","Spencer", "Gardner", "Stephens", "Payne",
		"Pierce","Berry","Matthews","Arnold","Wagner","Willis","Ray","Watkins","Olson","Carroll","Duncan","Snyder",
		"Hart", "Cunningham", "Bradley","Lane","Andrews","Harper", "Fox","Riley","Armstrong", "Carpenter","Weaver",
		"Greene","Lawrence", "Elliott","Chavez", "Sims","Austin", "Peters","Kelley", "Franklin","Lawson", "Fields",
		"Ryan", "Schmidt", "Carr", "Castillo", "Wheeler"
	};

	static String randomFirstName()
	{
		return FIRST_NAMES[(int)(Math.random() * FIRST_NAMES.length)];
	}

	static String randomLastName()
	{
		return LAST_NAMES[(int)(Math.random() * LAST_NAMES.length)];
	}

	/**
	 * poor man's genetics :D, results in roughly normally distributed heights at 1.75 m +/- 0.40 m
	 *
	 */
	static double randomPersonHeight()
	{
		return 1.25                // base height
			+ Math.random() * 0.10 // gene 1
			+ Math.random() * 0.15 // gene 2
			+ Math.random() * 0.20 // gene 3
			+ Math.random() * 0.25 // gene 4
			+ Math.random() * 0.30 // gene 5
		;
	}

	/**
	 * Linearily distributed date of birth from 1970 until now.
	 */
	static Date randomPersonDateOfBirth()
	{
		return new Date((long)(Math.random() * System.currentTimeMillis()));
	}

	/**
	 * Linearily distributed date of birth from {@code lowerBound} until now.
	 */
	static Date randomPersonDateOfBirth(final Date lowerBound)
	{
		return new Date(lowerBound.getTime() + (long)(Math.random() * (System.currentTimeMillis() - lowerBound.getTime())));
	}

	/**
	 * Linearily distributed date of birth from {@code lowerBound} until {@code upperBound}.
	 */
	static Date randomPersonDateOfBirth(final Date lowerBound, final Date upperBound)
	{
		return new Date(lowerBound.getTime() + (long)(Math.random() * (upperBound.getTime() - lowerBound.getTime())));
	}



//	public static void main(final String[] args)
//	{
//		final Date d = JadothTime.date(1900, 1, 1);
//		final Date d2 = JadothTime.date(1950, 12, 31);
//
//		for(int i = 0; i < 1000; i++)
//		{
////			System.out.println(randomPersonHeight());
////			System.out.println(randomPersonDateOfBirth().getYear());
////			System.out.println(randomPersonDateOfBirth(d).getYear());
////			System.out.println(randomPersonDateOfBirth(d, d2).getYear());
//		}
//	}

}
