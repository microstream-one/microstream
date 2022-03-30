package one.microstream.examples.customtypehandler;

/*-
 * #%L
 * microstream-examples-custom-type-handler
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

import java.util.Date;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryTypeHandler;

public class Employee
{
	/*
	 * The entity class must just contain "any" method returning a suitable type handler
	 * and MicroStream will recognize it and use the returned handler automatically.
	 * 
	 * Type type handler just needs to specify the entity class and define a list of fields
	 * comprised of (name, getter, setter) in arbitrary order.
	 */
	static BinaryTypeHandler<Employee> provideTypeHandler()
	{
		return Binary.TypeHandler(
			Employee.class,
			Binary.Field_long("id",
				e -> Long.parseLong(e.id),
				(e, value) -> e.id = String.valueOf(value)
			),
			Binary.Field_long("dateOfBirth",
				e -> e.dateOfBirth.getTime(),
				(e, value) -> e.dateOfBirth = new Date(value)
			),
			Binary.Field_double("salary",
				e -> e.salary.longValue(),
				(e, value) -> e.salary = Double.valueOf(value)
			)
		);
	}
	
	/*
	 * Fields with primitive data are (for whatever reason, e.g. project design rules) all object types,
	 * but records should be stored as efficient as possible, i.e. without overhead of references and value objects.
	 * 
	 * MicroStream's generic type analysis does not know of this and hence cannot do it.
	 * But defining a custom type handler can
	 */

	private String id         ;
	private Double salary     ;
	private Date   dateOfBirth;
		
	public Employee()
	{
		super();
	}	
	
	public Employee(String id, Double salary, Date dateOfBirth)
	{
		super();
		this.id          = id;
		this.salary      = salary;
		this.dateOfBirth = dateOfBirth;
	}

	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Double getSalary()
	{
		return this.salary;
	}

	public void setSalary(Double salary)
	{
		this.salary = salary;
	}

	public Date getDateOfBirth()
	{
		return this.dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}
}
