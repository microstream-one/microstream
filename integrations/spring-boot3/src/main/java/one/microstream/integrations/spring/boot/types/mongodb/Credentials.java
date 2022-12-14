package one.microstream.integrations.spring.boot.types.mongodb;

/*-
 * #%L
 * microstream-integrations-spring-boot3
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

public class Credentials
{

    /**
     * The type of the authentication mechanism. Supported values are:
     * <ul>
     * <li>"gssapi"</li>
     * Creates a MongoCredential instance for the GSSAPI SASL mechanism with the supplied "configuration.username" property. To override the default service name of "mongodb", add a mechanism property with the name "SERVICE_NAME". To force canonicalization of the host name prior to authentication, add a mechanism property with the name "CANONICALIZE_HOST_NAME" with the value true. To override the javax.security.auth.Subject with which the authentication executes, add a mechanism property with the name "JAVA_SUBJECT" with the value of a Subject instance. To override the properties of the javax.security.sasl.SaslClient with which the authentication executes, add a mechanism property with the name "JAVA_SASL_CLIENT_PROPERTIES" with the value of a Map<String, Object> instance containing the necessary properties. This can be useful if the application is customizing the default javax.security.sasl.SaslClientFactory.
     *
     * <li>"plain"</li>
     * Creates a MongoCredential instance for the PLAIN SASL mechanism. Credentials will be loaded from the credentials.username, credentials.source and credentials.password properties.
     *
     * <li>"mongodb-x509"</li>
     * Creates a MongoCredential instance for the MongoDB X.509 protocol with the supplied "configuration.username" property.
     *
     * <li>"mongo-cr"</li>
     * Creates a MongoCredential instance with an unspecified mechanism. The client will negotiate the best mechanism based on the version of the server that the client is authenticating to. Credentials will be loaded from the credentials.username and credentials.password properties.
     * </ul>
     */
    String authMechanism;

    /**
     * The username, used for various auth mechanisms.
     */
    String username;

    /**
     * The password, used for various auth mechanisms.
     */
    String password;

    /**
     * The source where the user is defined. This can be either "$external" or the name of a database. Used when credentials.auth-mechanism=plain.
     */
    String source;
}
