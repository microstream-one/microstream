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
package net.jadoth.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Thomas Muenz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface License
{

	/**
	 * Name.
	 *
	 * @return the string
	 */
	String name();

	/**
	 * Returns a string containing the version of the license in an appropriate manner for that license.<br>
	 * May be an empty string if no version information is applicable for that license.
	 * <p>
	 * <u>Examples</u>:<br>
	 * - "1.0"
	 * - "2010-01-01"
	 * -
	 * @return a
	 */
	String version() default "";


	/**
	 * Text.
	 *
	 * @return the license text of the license applicable for this type or an empty string if
	 *  returns a class that is annotated with the applicable license and acts as a central
	 * license holder.
	 */
	String text() default "";

	/**
	 * Declaring class.
	 *
	 * @return the  that is annotated by this annotation.
	 */
	Class<?> declaringClass();

	/**
	 * License class.
	 *
	 * @return the  that contains the full license information applicable for the type
	 * that is annotated by this annotation.
	 */
	Class<?> licenseClass() default License.class;

	/**
	 * Contains any URL (/URI) strings that are associated with the applied license of with the use of the license
	 * in this context.
	 *
	 * @return an array of URLs (/URIs) with additional information.
	 */
	String[] additionalLicenseInformationURLs() default {};


	/**
	 * Contains an arbitrary String containing any kind of additional information the author of the <code>Class</code>
	 * annotated with this annotation sees fit in conjunction with use of this annotation.
	 *
	 * @return an arbitrary String containing any kind of additional information.
	 */
	String additionalInformation() default "";


}
