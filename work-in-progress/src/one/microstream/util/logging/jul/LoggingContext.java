/**
 * 
 */
package one.microstream.util.logging.jul;

import java.util.logging.Level;

/**
 * @author Thomas Muenz
 *
 */
public interface LoggingContext
{
	/**
	 * Gets the logging aspect.
	 * 
	 * @return the logging aspect
	 */
	public LoggingAspect getLoggingAspect();
	
	/**
	 * Gets the level.
	 * 
	 * @return the level
	 */
	public Level getLevel();
	
	/**
	 * Gets the source class.
	 * 
	 * @return the source class
	 */
	public Class<?> getSourceClass();
	
	/**
	 * Gets the source method name.
	 * 
	 * @return the source method name
	 */
	public String getSourceMethodName();
}
