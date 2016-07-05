
// 1.) Cleanup inlined comments: "\{[ \t]*((//.*)|(/\*.*\*/))\R([ \t]*/*[ \t]*)" -> "{\R$4$1\R$4"
// 2.) Move Curly braces: "^([ \t]*/*\t*)(.*)([^\s/\*]+)[ \t]*\{[ \t]*\R+" -> "$1$2$3\R$1{\R"

public class BlockOpeningRefactoringTest{

	static final int[] ARRAY1a = {1, 2, 3};
	static final int[] ARRAY2a = new int[]{1, 2, 3};

	static final int[] ARRAY1b = {
		1,
		2,
		3
	};

	static final int[] ARRAY2b = new int[]{
		1,
		2,
		3
	};

	static final int[] ARRAY1c = {
		1, 2, 3
	};

	static final int[] ARRAY2c = new int[]{
		1, 2, 3
	};


	/**
	 * Refactoring test comment:
	 *
	 * Equivalent of
	 * t -> {
	 *     return true;
	 * }
	 *
	 */
	static final java.util.function.Predicate<Object> SOME_TEST = new java.util.function.Predicate<Object>()  	  {
		@Override public boolean test(final Object t) {
			return true;
		}

	};

	static final java.util.function.Predicate<Object> MEAN_BLANK_LINE_TEST = new java.util.function.Predicate<Object>(){

		@Override public boolean test(final Object t) {



			return true;
		}

	};


	public static void main(final String[] args) throws Exception
	{
		// simple if
		if(true){



			System.out.println("bla");
		}

		// if-else
		if(System.currentTimeMillis() > 1000 ){
			System.out.println("bla");
		}
		else if(System.currentTimeMillis() > 0){
			System.out.println("blub");
		}
		else {
			System.out.println("blub");
		}

		// more complex example
		if (System.currentTimeMillis() > 0 && (System.currentTimeMillis() > 5 || System.currentTimeMillis() > 10)){
			System.out.println("bla");

			// deeper indentation and arbitrary blanks
			if (true)          {
				System.out.println("bla");

				// we have to go deeper!
				if(true) {
					System.out.println("deep");
				}
			}
		}

		// try-catch cases
		try {
			System.gc();
		}
		catch(final RuntimeException e){
			System.err.println("blood death explosions");
		}
		catch(final Error e) { /* ignore intentionally */}
		finally{
			System.out.println("who cares");
		}

		// try-with-resources
		try (java.io.BufferedReader br = new java.io.BufferedReader(null)) {
			br.readLine();
		}

		// switch
		switch(args.length){
			// case ignored intentionally for now
			case 1: {
				System.out.println("one argument");
				break;
			}
			case 2:
				break;
			default:
				throw new RuntimeException("Invalid number of arguments.");
		}

		// mean case
//		if(true) {
//			System.out.println("bla");
//		}

		// very mean case
	//	if(true) {
	//		System.out.println("bla");
	//	}

		if(true) {
			// some comment
			System.out.println("bla");
		}

		if(true) {
//			System.out.println("bla");
			System.out.println("blub");
		}


//		if(true) {
//			// some comment
//			System.out.println("bla");
//		}

//		if(true) {
////		System.out.println("bla");
//			System.out.println("blub");
//		}

	//	if(true) {
	////	System.out.println("bla");
	//		System.out.println("blub");
	//	}

		if(true){ // nasty inlined comment
			System.out.println("bla");
		}

//		if(true){ // nasty inlined comment
//			System.out.println("bla");
//		}

		if(true){  	   /* nasty inlined comment */
			System.out.println("bla");
		}

//		if(true){	/* nasty inlined comment */
//			System.out.println("bla");
//		}

		// already refactored case
		if(true)
		{
			System.out.println("bla");
		}

		// already refactored case, commtented out
//		if(true)
//		{
//			System.out.println("bla");
//		}

		// already refactored case, commtented out
/* Some comment explaining something and using a code snippet as an example
 * {
 *     System.out.println("bla");
 * }
 */

	}

}
