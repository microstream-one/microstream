/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.util.logging.jul;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Simple Wrapper around java.util.logging.Logger with little logic of its own, just to reduce the amount of code
 * that needs to be written for logging (by ~50%).<br>
 * This is achieved by assigning a default level to a logger that can be used for every message.
 * <ul>
 * <li>This way, it can be saved to refactor the Level (or method name respectively) in every occurance of log().
 * <li>In addition, checks like logger.isLoggable(LoggingLevels.CATEGORY_MYLEVEL) can be reduced to logger.isLoggable()
 * <li>Also, a log(etc, Object... parameters) method is provided to further save boiler plate code like new Object[]{...
 * </ul>
 * <p>
 * Examples:<br>
 * <code>
 * logger.log(Level.FINEST, "Detailed message");<br>
 *  |<br>
 *  V<br>
 * logger.log("Detailed message");<br>
 * <br>
 * <br>
 * <br>
 * if(logger.isLoggable(LoggingLevels.CATEGORY_MYLEVEL))
 * logger.log(LoggingLevels.CATEGORY_MYLEVEL, "Detailed message");<br>
 *  |<br>
 *  V<br>
 * if(logger.isLoggable()) logger.log("Detailed message");<br>
 * <br>
 * <br>
 * <br>
 * logger.log(Level.FINEST, "Detailed message: {0}, {1}, {2}", new Object[]{a,b,c});<br>
 *  |<br>
 *  V<br>
 * logger.log("Detailed message: {0}, {1}, {2}", a,b,c);<br>
 * </code>
 * <br>
 * <br>
 * The name was chosen for lack of significant synonyms for classes dealing generally with logging
 * ("Logger", "Logging", etc.) and in inspiration by Aspect Orientation (and maybe future use of this class in such
 * a way. See interface {@link LoggingContext})
 *
 *
 * @author Thomas Muenz
 *
 */
public class LoggingAspect
{

	/**
	 * This is the best effort with object oriented techniques to abstract the logging aspect.
	 *
	 * @param context the context
	 * @param method the method
	 * @param message the message
	 */
	public static void log(final LoggingContext context, final String method, final CharSequence message)
	{
		final LoggingAspect logger = context.getLoggingAspect();
		if(logger == null || !logger.isEnabled())
		{
			return;
		}

		final Level level = context.getLevel();
		// toString() may be expensive, so check level first. If level is null, the defaultLevel will be checked.
		if(!logger.checkLevel(level))
		{
			return;
		}

		logger.logB(level, message.toString(), context.getSourceClass(), method);
	}

	/**
	 * Log.
	 *
	 * @param context the context
	 * @param message the message
	 */
	public static void log(final LoggingContext context, final CharSequence message)
	{
		if(context == null)
		{
			return;
		}
		log(context, context.getSourceMethodName(), message);
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/** The logger. */
	private Logger logger;

	/** The default level. */
	private Level defaultLevel;

	/** The source class name. */
	private String sourceClassName;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Instantiates a new logging aspect.
	 *
	 * @param logger the logger to which <code>defaultLevel</code> will be associated. My not be null.
	 * @param defaultLevel the default level for <code>logger</code>. My not be null.
	 */
	public LoggingAspect(final Logger logger, final Level defaultLevel)
	{
		this(logger, defaultLevel, null);
		this.sourceClassName = this.determineSourceClassName();
	}

	/**
	 * Instantiates a new logging aspect.
	 *
	 * @param logger the logger to which <code>defaultLevel</code> will be associated.
	 * @param defaultLevel the default level for <code>logger</code>. My not be null.
	 * @param caller the caller
	 */
	public LoggingAspect(final Logger logger, final Level defaultLevel, final Class<?> caller)
	{
		if(defaultLevel == null)
		{
			throw new NullPointerException("defaultLevel is null");
		}
		this.logger = logger;
		this.defaultLevel = defaultLevel;
		this.sourceClassName = caller == null ? null : caller.getName();
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////
	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public Logger getLogger()
	{
		return this.logger;
	}

	/**
	 * Gets the default level.
	 *
	 * @return the defaultLevel
	 */
	public Level getDefaultLevel()
	{
		return this.defaultLevel;
	}

	/**
	 * Gets the source class name.
	 *
	 * @return the sourceClassName
	 */
	public String getSourceClassName()
	{
		return this.sourceClassName;
	}



	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////
	/**
	 * Sets the source class name.
	 *
	 * @param sourceClassName the sourceClassName to set
	 */
	public void setSourceClassName(final String sourceClassName)
	{
		this.sourceClassName = sourceClassName;
	}
	/**
	 * @param logger the logger to set
	 */
	public void setLogger(final Logger logger)
	{
		this.logger = logger;
	}

	/**
	 * @param defaultLevel the defaultLevel to set
	 */
	public void setDefaultLevel(final Level defaultLevel)
	{
		this.defaultLevel = defaultLevel;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////
	/**
	 * Checks if is enabled.
	 *
	 * @return true, if is enabled
	 */
	public boolean isEnabled()
	{
		return this.logger != null;
	}

	/**
	 * Checks if is loggable.
	 *
	 * @return true, if is loggable
	 */
	public boolean isLoggable()
	{
		return this.logger != null && this.logger.isLoggable(this.defaultLevel);
	}

	/**
	 * Checks if is loggable.
	 *
	 * @param level the level
	 * @return true, if is loggable
	 */
	public boolean isLoggable(final Level level)
	{
		return this.logger != null && this.logger.isLoggable(level == null ? this.defaultLevel : level);
	}

	/**
	 * Check level.
	 *
	 * @return true, if successful
	 */
	public boolean checkLevel()
	{
		return this.logger.isLoggable(this.defaultLevel);
	}

	/**
	 * Check level.
	 *
	 * @param level the level
	 * @return true, if successful
	 */
	public boolean checkLevel(final Level level)
	{
		return this.logger.isLoggable(level == null ? this.defaultLevel : level);
	}


	/**
	 * Log.
	 *
	 * @param record the record
	 */
	public void log(final LogRecord record)
	{
		this.logger.log(record);
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Log.
	 *
	 * @param message the message
	 * @param parameters the parameters
	 */
	public void log(final String message, final Object... parameters)
	{
		this.logger.logp(this.defaultLevel, this.sourceClassName, null, message, parameters);
	}

	/**
	 * Log.
	 *
	 * @param message the message
	 * @param thrown the thrown
	 */
	public void log(final String message, final Throwable thrown)
	{
		this.logger.logp(this.defaultLevel, this.sourceClassName, null, message, thrown);
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Log.
	 *
	 * @param level the level
	 * @param message the message
	 * @param parameters the parameters
	 */
	public void log(final Level level, final String message, final Object... parameters)
	{
		this.logger.logp(level, this.sourceClassName, null, message, parameters);
	}

	/**
	 * Log.
	 *
	 * @param level the level
	 * @param message the message
	 * @param thrown the thrown
	 */
	public void log(final Level level, final String message, final Throwable thrown)
	{
		this.logger.logp(level, this.sourceClassName, null, message, thrown);
	}

	/**
	 * Log.
	 *
	 * @param level the level
	 * @param thrown the thrown
	 */
	public void log(final Level level, final Throwable thrown)
	{
		this.logger.logp(level, this.sourceClassName, null, thrown == null ? null : thrown.getMessage(), thrown);
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Log a.
	 *
	 * @param message the message
	 * @param parameters the parameters
	 */
	public void logA(final String message, final Object... parameters)
	{
		final String sourceClassName = this.sourceClassName != null
			? this.sourceClassName
			: this.determineSourceClassName()
		;
		this.logger.logp(this.defaultLevel, sourceClassName, this.determineSourceMethodName(), message, parameters);
	}

	/**
	 * Log a.
	 *
	 * @param message the message
	 * @param thrown the thrown
	 */
	public void logA(final String message, final Throwable thrown)
	{
		final String sourceClassName = this.sourceClassName != null
			? this.sourceClassName
			: this.determineSourceClassName()
		;
		this.logger.logp(this.defaultLevel, sourceClassName, this.determineSourceMethodName(), message, thrown);
	}

	/**
	 * Log a.
	 *
	 * @param level the level
	 * @param message the message
	 * @param parameters the parameters
	 */
	public void logA(final Level level, final String message, final Object... parameters)
	{
		final String sourceClassName = this.sourceClassName != null
			? this.sourceClassName
			: this.determineSourceClassName()
		;
		this.logger.logp(level, sourceClassName, this.determineSourceMethodName(), message, parameters);
	}

	/**
	 * Log a.
	 *
	 * @param level the level
	 * @param message the message
	 * @param thrown the thrown
	 */
	public void logA(final Level level, final String message, final Throwable thrown)
	{
		final String sourceClassName = this.sourceClassName != null
			? this.sourceClassName
			: this.determineSourceClassName()
		;
		this.logger.logp(level, sourceClassName, this.determineSourceMethodName(), message, thrown);
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Log m.
	 *
	 * @param message the message
	 * @param methodName the method name
	 * @param parameters the parameters
	 */
	public void logM(final String message, final String methodName, final Object... parameters)
	{
		this.logger.logp(this.defaultLevel, this.sourceClassName, methodName, message, parameters);
	}

	/**
	 * Log m.
	 *
	 * @param message the message
	 * @param methodName the method name
	 * @param thrown the thrown
	 */
	public void logM(final String message, final String methodName, final Throwable thrown)
	{
		this.logger.logp(this.defaultLevel, this.sourceClassName, methodName, message, thrown);
	}

	/**
	 * Log m.
	 *
	 * @param level the level
	 * @param message the message
	 * @param methodName the method name
	 * @param parameters the parameters
	 */
	public void logM(final Level level, final String message, final String methodName, final Object... parameters)
	{
		this.logger.logp(level, this.sourceClassName, methodName, message, parameters);
	}

	/**
	 * Log m.
	 *
	 * @param level the level
	 * @param message the message
	 * @param methodName the method name
	 * @param thrown the thrown
	 */
	public void logM(final Level level, final String message, final String methodName, final Throwable thrown)
	{
		this.logger.logp(level, this.sourceClassName, methodName, message, thrown);
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Log c.
	 *
	 * @param message the message
	 * @param sourceClass the source class
	 * @param parameters the parameters
	 */
	public void logC(final String message, final Class<?> sourceClass, final Object... parameters)
	{
		this.logger.logp(this.defaultLevel, sourceClass == null
			? null
			: sourceClass.getName(), null, message, parameters)
		;
	}

	/**
	 * Log c.
	 *
	 * @param message the message
	 * @param sourceClass the source class
	 * @param thrown the thrown
	 */
	public void logC(final String message, final Class<?> sourceClass, final Throwable thrown)
	{
		this.logger.logp(this.defaultLevel, sourceClass == null ? null : sourceClass.getName(), null, message, thrown);
	}

	/**
	 * Log c.
	 *
	 * @param level the level
	 * @param message the message
	 * @param sourceClass the source class
	 * @param parameters the parameters
	 */
	public void logC(final Level level, final String message, final Class<?> sourceClass, final Object... parameters)
	{
		this.logger.logp(level, sourceClass == null ? null : sourceClass.getName(), null, message, parameters);
	}

	/**
	 * Log c.
	 *
	 * @param level the level
	 * @param message the message
	 * @param sourceClass the source class
	 * @param thrown the thrown
	 */
	public void logC(final Level level, final String message, final Class<?> sourceClass, final Throwable thrown)
	{
		this.logger.logp(level, sourceClass == null ? null : sourceClass.getName(), null, message, thrown);
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Log b.
	 *
	 * @param level the level
	 * @param message the message
	 * @param sourceClass the source class
	 * @param methodName the method name
	 * @param parameters the parameters
	 */
	public void logB(
		final Level     level      ,
		final String    message    ,
		final Class<?>  sourceClass,
		final String    methodName ,
		final Object... parameters
	)
	{
		this.logger.logp(level, sourceClass == null ? null : sourceClass.getName(), methodName, message, parameters);
	}

	/**
	 * Log b.
	 *
	 * @param level the level
	 * @param message the message
	 * @param sourceClass the source class
	 * @param methodName the method name
	 * @param thrown the thrown
	 */
	public void logB(
		final Level     level      ,
		final String    message    ,
		final Class<?>  sourceClass,
		final String    methodName ,
		final Throwable thrown
	)
	{
		this.logger.logp(level, sourceClass == null ? null : sourceClass.getName(), methodName, message, thrown);
	}

	///////////////////////////////////////////////////////////////////////////
//	public void logp(Level level, String sourceClassName, String methodName, String message, Object... parameters)
//	{
//		this.logger.logp(level, sourceClassName, methodName, message, parameters);
//	}
//	public void logp(Level level, String sourceClassName, String methodName, String message, Throwable thrown)
//		{
//		this.logger.logp(level, sourceClassName, methodName, message, thrown);
//	}
	///////////////////////////////////////////////////////////////////////////



	/**
	 * Determine source method name.
	 *
	 * @return the string
	 */
	protected String determineSourceMethodName()
	{
		final StackTraceElement[] stack = new Throwable().getStackTrace();
		final String thisClassName = this.getClass().getName();

		for(final StackTraceElement e : stack)
		{
			if(!e.getClassName().equals(thisClassName))
			{
				return e.getMethodName();
			}
		}

		return null;
	}

	/**
	 * Determine source class name.
	 *
	 * @return the string
	 */
	public String determineSourceClassName()
	{
		final StackTraceElement[] stack = new Throwable().getStackTrace();
		final String thisClassName = this.getClass().getName();

		String currentClassName = null;
		for(final StackTraceElement e : stack)
		{
			currentClassName = e.getClassName();
			if(!currentClassName.equals(thisClassName))
			{
				return currentClassName;
			}
		}
		return thisClassName;
	}

}
