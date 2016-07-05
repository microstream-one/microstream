package net.jadoth.test.reflection;

import net.jadoth.test.reflection.copy.TestAnnotation;

/**
 * @author Thomas Muenz
 *
 */
public class MySubClassA1 extends MySubClassA {

	public Boolean subClassA1Value = true;

	@TestAnnotation
	public Boolean annotatedSubClassA1Value = true;
}
