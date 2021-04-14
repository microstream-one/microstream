/**
 * 
 */
package one.microstream.functional;

/**
 * 
 *
 */
/*
 * "Consumer" in the JDK is an hilariously wrong term for the used pattern.
 * The passed value is NOT "consumed" (absorbed, removed, deleted).
 * The used pattern is merely a piece of logic that is applied ("visits") the passed value, but does NOT consume it.
 * "Procedure" is MUCH more fitting.
 */
public interface _charProcedure
{
	public void accept(char c);
}
