package one.microstream.integrations.spring.boot.types.aws;

/*-
 * #%L
 * microstream-integrations-spring-boot
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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
     * The type of the credentials' provider. Supported values are:
     * <ul>
     *
     * <li>"environment-variables" Credentials will be loaded from the AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY and AWS_SESSION_TOKEN environment variables.</li>
     * <li>"system-properties" Credentials will be loaded from the aws.accessKeyId, aws.secretAccessKey and aws.sessionToken system properties.</li>
     * <li>"static" Credentials will be loaded from the credentials.access-key-id and credentials.secret-access-key properties.</li>
     * <li>"default" Credentials provider chain that looks for credentials in this order:</li>
     * <ol>
     * <li>Java System Properties - aws.accessKeyId and aws.secretKey</liv>
     * <li>Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY</li>
     * <li>Credential profiles file at the default location (~/.aws/credentials) shared by all AWS SDKs and the AWS CLI</li>
     * <li>Credentials delivered through the Amazon EC2 container service if AWS_CONTAINER_CREDENTIALS_RELATIVE_URI" environment variable is set and security manager has permission to access the variable</li>
     * <li>Instance profile credentials delivered through the Amazon EC2 metadata service</li>
     * </ol>
     * </ul>
     */
    private String type;

    /**
     * The access key id, used when "credentials.type" is "static".
     */
    private String accessKeyId;

    /**
     * The secret access key, used when "credentials.type" is "static".
     */
    private String secretAccessKey;


    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getAccessKeyId()
    {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId)
    {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey()
    {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey)
    {
        this.secretAccessKey = secretAccessKey;
    }
}
