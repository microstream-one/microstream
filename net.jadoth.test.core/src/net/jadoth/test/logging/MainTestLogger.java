/**
 * 
 */
package net.jadoth.test.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.jadoth.util.logging.jul.LoggingAspect;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestLogger 
{
	
	static LoggingAspect logger = new LoggingAspect(Logger.getLogger("test"), Level.SEVERE, null);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.log("test message");
	}

}
