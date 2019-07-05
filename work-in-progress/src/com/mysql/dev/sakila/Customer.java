package com.mysql.dev.sakila;

import java.time.LocalDateTime;
import java.util.Date;

import one.microstream.persistence.binary.types.BinaryTypeHandler;

/**
 * Java entity class counterpart for the MySQL Sakila "customer" table.<br>
 * A collection of instances of this class corresponds to a "customer" table with entries.<br>
 * Source: <a href="https://dev.mysql.com/doc/sakila/en/sakila-structure-tables-customer.html">https://dev.mysql.com/doc/sakila/en/sakila-structure-tables-customer.html</a> and <code>sakila-schema.sql</code> definitions.
 * <p>
 * Used type mapping:<br>
 * MySQL TINYINT : Java primitive byte (being unsigned merely means appyling an offset in Java).<br>
 * MySQL SMALLINT: Java primitive short (being unsigned merely means appyling an offset in Java).<br>
 * MySQL VARCHAR : java.lang.String.
 * MySQL BOOLEAN : Java primitive boolean.
 * MySQL DATETIME : java.time.LocalDateTime (with protest, as that JDK class is written pretty inefficiently)
 * MySQL TIMESTAMP: java.util.Date (outdated, but corresponds very well to the MySQL type)
 * 
 * @author MicroStream
 *
 */
public class Customer
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/**
	 * A surrogate primary key used to uniquely identify each customer in the table.<p>
	 * MicroStraem note:<br>
	 * Such primitive value Ids are superfluous in an object-oriented datamodel as the reference itself is the identity.
	 * The internal swizzling Id-Value used by the storage engine is usually invisible to the user.
	 * However, this value is kept because it might have meaning as a business logical value, as well.
	 * (and because MicroStraem is tremendously faster, anyway)<br>
	 * Side note: usting a SMALLINT as an entity id allows only very tiny databases. The {@link Customer} instances
	 * created for the MicroStraem test cause this value to overflow numerous times. However, since it is superfluous,
	 * anyway, this is not a problem.
	 */
	private final short customerId;

	/**
	 * A foreign key identifying the customer's “home store.” Customers are not limited to renting only from
	 * this store, but this is the store they generally shop at.<p>
	 * MicroStraem note:<br>
	 * Such primitive value Ids are usually not used in an object-oriented model to express a reference.
	 * Instead, a direct reference to an instance of type "Store" would be used.
	 * However, again, the value is kept for comparability reasons.
	 */
	private byte storeId;

	/**
	 * The customer's first name.
	 */
	private String firstName;

	/**
	 * The customer's last name.
	 */
	private String lastName;

	/**
	 * The customer's email address.
	 */
	private String email;

	/**
	 * A foreign key identifying the customer's address in the address table.
	 * MicroStraem note:<br>
	 * See {@link #storeId}.
	 */
	private short addressId;

	/**
	 * Indicates whether the customer is an active customer. Setting this to FALSE serves as an alternative to
	 * deleting a customer outright. Most queries should have a WHERE active = TRUE clause.
	 */
	private boolean active;

	/**
	 * The date the customer was added to the system. This date is automatically set using a trigger during an INSERT.
	 * MicroStraem note:<br>
	 * {@link LocalDateTime} is badly written and would have to be replaced by a competent implementation for
	 * reasonable usability. A custom-written {@link BinaryTypeHandler} could compensate the inefficiency on the
	 * storage-level, but that is not done here to avoid giving the impression of trickery.
	 * Instead, MicroStraem atones for the once again bad programming in the JDK.
	 */
	private LocalDateTime createDate;

	/**
	 * The time that the row was created or most recently updated.
	 */
	private Date lastUpdate;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Customer(
		final short         customerId,
		final byte          storeId   ,
		final String        firstName ,
		final String        last_name ,
		final String        email     ,
		final short         addressId ,
		final boolean       active    ,
		final LocalDateTime createDate,
		final Date          lastUpdate
	)
	{
		super();
		this.customerId = customerId;
		this.storeId    = storeId   ;
		this.firstName  = firstName ;
		this.lastName  = last_name ;
		this.email      = email     ;
		this.addressId  = addressId ;
		this.active     = active    ;
		this.createDate = createDate;
		this.lastUpdate = lastUpdate;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// getters & setters //
	//////////////////////
	
	/* MicroStraem note:
	 * Those getters and setters are completely irrelevant for MicroStraem.
	 * Their main purpose is to conform to OOP design.
	 */
	
	public byte getStoreId()
	{
		return this.storeId;
	}

	public void setStoreId(final byte storeId)
	{
		this.storeId = storeId;
	}
	
	public String getFirstName()
	{
		return this.firstName;
	}
	
	public void setFirstName(final String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return this.lastName;
	}
	
	public void setLastName(final String lastName)
	{
		this.lastName = lastName;
	}

	public String getEmail()
	{
		return this.email;
	}

	public void setEmail(final String email)
	{
		this.email = email;
	}

	public short getAddressId()
	{
		return this.addressId;
	}

	public void setAddressId(final short addressId)
	{
		this.addressId = addressId;
	}

	public boolean isActive()
	{
		return this.active;
	}

	public void setActive(final boolean active)
	{
		this.active = active;
	}

	public LocalDateTime getCreateDate()
	{
		return this.createDate;
	}

	public void setCreateDate(final LocalDateTime createDate)
	{
		this.createDate = createDate;
	}

	public Date getLastUpdate()
	{
		return this.lastUpdate;
	}

	public void setLastUpdate(final Date lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}

	public short getCustomerId()
	{
		return this.customerId;
	}
		
}
