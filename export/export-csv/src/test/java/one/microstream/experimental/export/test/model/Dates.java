package one.microstream.experimental.export.test.model;

/*-
 * #%L
 * export
 * %%
 * Copyright (C) 2023 MicroStream Software
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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class Dates {

    private Date aDate;
    private LocalDate aLocalDate;
    private LocalDateTime aLocalDateTime;
    private Instant anInstant;
    private Timestamp aTimestamp;

    public Date getaDate() {
        return aDate;
    }

    public void setaDate(final Date aDate) {
        this.aDate = aDate;
    }

    public LocalDate getaLocalDate() {
        return aLocalDate;
    }

    public void setaLocalDate(final LocalDate aLocalDate) {
        this.aLocalDate = aLocalDate;
    }

    public LocalDateTime getaLocalDateTime() {
        return aLocalDateTime;
    }

    public void setaLocalDateTime(final LocalDateTime aLocalDateTime) {
        this.aLocalDateTime = aLocalDateTime;
    }

    public Instant getAnInstant() {
        return anInstant;
    }

    public void setAnInstant(final Instant anInstant) {
        this.anInstant = anInstant;
    }

    public Timestamp getaTimestamp() {
        return aTimestamp;
    }

    public void setaTimestamp(final Timestamp aTimestamp) {
        this.aTimestamp = aTimestamp;
    }
}
