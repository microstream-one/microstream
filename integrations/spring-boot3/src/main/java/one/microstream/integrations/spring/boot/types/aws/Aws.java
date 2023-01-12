package one.microstream.integrations.spring.boot.types.aws;

/*-
 * #%L
 * microstream-integrations-spring-boot-common
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

import org.springframework.boot.context.properties.NestedConfigurationProperty;

public class Aws
{

    @NestedConfigurationProperty
    private Dynamodb dynamodb;

    @NestedConfigurationProperty
    private S3 s3;

    public Dynamodb getDynamodb()
    {
        return this.dynamodb;
    }

    public void setDynamodb(final Dynamodb dynamodb)
    {
        this.dynamodb = dynamodb;
    }

    public S3 getS3()
    {
        return this.s3;
    }

    public void setS3(final S3 s3)
    {
        this.s3 = s3;
    }
}
