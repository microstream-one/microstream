package one.microstream.integrations.spring.types;

/*-
 * #%L
 * microstream-spring
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

import one.microstream.integrations.spring.boot.types.MicroStreamConfiguration;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


public class AutoConfTest {

    @TempDir
    Path tempDir;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MicroStreamConfiguration.class));

    @Test
    public void defaultServiceBacksOff() {
        String path = tempDir.toAbsolutePath().toString();
        this.contextRunner.withUserConfiguration(MicroStreamConfiguration.class)
                .withPropertyValues("one.microstream.storage-directory=" + path)
                .run((context) ->
                {
                    assertThat(context).hasSingleBean(EmbeddedStorageManager.class);
                    assertThat(context.getBean(EmbeddedStorageManager.class).toString()).isNotNull();
                });
    }

}
