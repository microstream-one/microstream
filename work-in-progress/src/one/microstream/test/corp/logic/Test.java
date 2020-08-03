package one.microstream.test.corp.logic;

import static one.microstream.X.notNull;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.function.Function;

import one.microstream.X;
import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XTable;
import one.microstream.io.XIO;
import one.microstream.math.XMath;
import one.microstream.meta.XDebug;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageLiveFileProvider;
import one.microstream.storage.types.StorageTransactionsAnalysis;
import one.microstream.test.corp.model.Address;
import one.microstream.test.corp.model.BusinessYear;
import one.microstream.test.corp.model.CallAddress;
import one.microstream.test.corp.model.City;
import one.microstream.test.corp.model.ClientCorporation;
import one.microstream.test.corp.model.Contact;
import one.microstream.test.corp.model.CorporateCustomer;
import one.microstream.test.corp.model.Corporation;
import one.microstream.test.corp.model.Customer;
import one.microstream.test.corp.model.EMailAddress;
import one.microstream.test.corp.model.Order;
import one.microstream.test.corp.model.OrderItem;
import one.microstream.test.corp.model.Person;
import one.microstream.test.corp.model.PhoneNumber;
import one.microstream.test.corp.model.PostalAddress;
import one.microstream.test.corp.model.PrivateCustomer;
import one.microstream.test.corp.model.Product;
import one.microstream.test.corp.model.Street;
import one.microstream.test.corp.model.Vendor;
import one.microstream.time.XTime;

public class Test
{
	// sum of all percentual values should yield 100.0 for predictable results
	static final GenerationAmount
		AMOUNT_EMailAddress      = amount( 8.0),
		AMOUNT_PhoneNumber       = amount( 8.0),
		AMOUNT_PostalAddress     = amount( 8.0),
		AMOUNT_CallAddress       = amount( 8.0),
		AMOUNT_Street            = amount( 4.0),
		AMOUNT_City              = amount( 1.0),
		AMOUNT_Address           = amount( 8.0),
		AMOUNT_Vendor            = amount( 1.0),
		AMOUNT_Product           = amount(10.0),
		AMOUNT_Person            = amount( 6.0),
		AMOUNT_Corporation       = amount( 2.0),
		AMOUNT_PrivateCustomer   = amount( 4.0),
		AMOUNT_CorporateCustomer = amount( 1.0),
		AMOUNT_Order             = amount(13.0),
		AMOUNT_OrderItem         = amount(18.0),
		AMOUNT_BusinessYear      = amount( 0.0, 5)
//		AMOUNT_ClientCorporation = amount( 0.0, 1)
	;

	static final GenerationAmount amount(final double fraction, final int minimumAmount)
	{
		return new GenerationAmount(fraction, minimumAmount);
	}

	static final GenerationAmount amount(final double fraction)
	{
		return amount(fraction, 1);
	}

	// single entity factory methods //

	public static EMailAddress EMailAddress(final String number)
	{
		return new EMailAddress("name"+number+"@somewhere.de", "E-Mail description "+number);
	}

	public static PhoneNumber PhoneNumber(final String number)
	{
		return new PhoneNumber("123 45 "+number, "Phone number description "+number);
	}

	public static City City(final String number)
	{
		return new City("City "+number);
	}

	public static Street Street(final City city, final String number)
	{
		return new Street(number+"thStreet", city);
	}

	public static PostalAddress PostalAddress(final Street street, final String number)
	{
		return new PostalAddress(street, number);
	}

	public static CallAddress CallAddress(final String number)
	{
		return new CallAddress(
			PhoneNumber (number),
			EMailAddress(number)
		);
	}

	public static Address Address(
		final Contact       owner        ,
		final PostalAddress postalAddress,
		final CallAddress   callAddress
	)
	{
		return new Address(owner, postalAddress, callAddress);
	}

	public static Product Product(final String productName, final Vendor vendor, final Double price)
	{
		return new Product(productName, vendor, price);
	}

	public static Person Person(
		final String  contactId,
		final String  firstname,
		final String  lastname,
		final Address address
	)
	{
		return new Person(contactId, firstname, lastname, address);
	}

	public static Corporation Corporation(
		final String  contactId,
		final String  name     ,
		final String  taxId    ,
		final Person  contact  ,
		final Address address
	)
	{
		return new Corporation(contactId, name, taxId, contact, address);
	}

	public static ClientCorporation ClientCorporation(
		final String  contactId,
		final String  name     ,
		final String  taxId    ,
		final Person  contact  ,
		final Address address
	)
	{
		return new ClientCorporation(contactId, name, taxId, contact, address);
	}

	public static PrivateCustomer PrivateCustomer(final Person person)
	{
		return new PrivateCustomer(person);
	}

	public static CorporateCustomer CorporateCustomer(final Corporation corporation)
	{
		return new CorporateCustomer(corporation);
	}

	public static Vendor Vendor(final Corporation corporation)
	{
		return new Vendor(corporation);
	}

	public static Order Order(
		final String                     orderId        ,
		final Customer                   customer       ,
		final XTable<Product, OrderItem> items          ,
		final Address                    billingAddress ,
		final Address                    shippingAddress
	)
	{
		return new Order(orderId, customer, items, billingAddress, shippingAddress);
	}

	public static OrderItem OrderItem(final Product product, final Integer amount, final Double totalPrice)
	{
		return new OrderItem(product, amount, totalPrice);
	}

	public static BusinessYear BusinessYear(final ClientCorporation owner , final Integer year)
	{
		return new BusinessYear(owner, year, EqHashTable.New());
	}



	// entity array factory methods //


	public static Object[] generateModelDataTrivial(final int entityAmount)
	{
		final Object[] array = new Object[entityAmount];
		for(int i = 0; i < array.length; i++)
		{
			array[i] = "value"+i;
		}
		return array;
	}
	
	public static void clearDefaultStorageDirectory()
	{
		clearDefaultStorageDirectory(true);
	}
	
	public static void clearDefaultStorageDirectory(final boolean output)
	{
		XDebug.deleteAllFiles(XIO.Path(StorageLiveFileProvider.Defaults.defaultStorageDirectory()), output);
	}

	public static ClientCorporation generateModelData(final int entityAmount)
	{
		final Generator generator = new Generator(entityAmount);
		print("Entity amounts: \n"+generator.infoGenerationAmounts());

		print("Generating model data ...");
		final ClientCorporation clientcorporation = generator.generateModelData();
		print("Model data generation complete.");

		return clientcorporation;
	}
	
	// test for BinaryHandlerHashSet implementation
	public static Object generateHashSet(final int entityAmount)
	{
		final HashSet<EqualityTest> elements = new HashSet<>(entityAmount);
		
		for(int i = 0; i < entityAmount; i++)
		{
			elements.add(new EqualityTest(i));
		}
		
		return elements;
	}
	static final class EqualityTest
	{
		final int state;

		public EqualityTest(final int state)
		{
			super();
			this.state = state;
		}
		
		@Override
		public int hashCode()
		{
			return this.state;
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			return obj == this
				|| obj instanceof EqualityTest
				&& this.state == ((EqualityTest)obj).state
			;
		}
		
	}

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

	public static void printInitializationTime(final EmbeddedStorageManager storage)
	{
		System.out.println(TIME_FORMAT.format(new Date(storage.initializationTime())) + ": Initializing database ...");
	}

	public static void printOperationModeTime(final EmbeddedStorageManager storage)
	{
		System.out.println(TIME_FORMAT.format(new Date(storage.operationModeTime())) + ": Database initialized.");
	}
	
	public static void print(final Object object)
	{
		System.out.println(TIME_FORMAT.format(XTime.now())+": "+object);
	}

	public static ADirectory provideTimestampedDirectory(final ADirectory directory, final String prefix)
	{
		final String fileName = prefix + "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.S").format(new Date());
		
		return directory == null
			? NioFileSystem.New().ensureDirectory(XIO.Path(fileName))
			: directory.ensureDirectory(fileName)
		;
	}

	public static ADirectory provideTimestampedDirectory(final String prefix)
	{
		return provideTimestampedDirectory(null, prefix);
	}


	public static void printTransactionsFiles(final AFile... files)
	{
		for(final AFile file : files)
		{
			printTransactionsFile(file);
		}
	}

	public static String assembleTransactionsFile(final AFile file)
	{
		final VarString vs = VarString.New(file.toString()).lf();
		StorageTransactionsAnalysis.EntryAssembler.assembleHeader(vs, "\t").lf();
		final VarString s = StorageTransactionsAnalysis.Logic.parseFile(file, vs)
			.lf().lf()
		;
		return s.toString();
	}
	
	public static void printTransactionsFile(final AFile file)
	{
		final String s = assembleTransactionsFile(file);
		System.out.println(s.toString());
	}

	public static void printTransactionsFiles(final ADirectory storageDirectory, final int channelCount)
	{
		final AFile[] files = new AFile[channelCount];

		for(int i = 0; i < files.length; i++)
		{
			files[i] = storageDirectory.ensureDirectory("channel_"+i).ensureFile("transactions_"+i+".sft");
		}
		printTransactionsFiles(files);
	}

	static final Function<Class<?>, HashEnum<Object>> supplier = k -> HashEnum.New();
	static final HashTable<Class<?>, HashEnum<Object>> registry = HashTable.New();
	static final boolean register(final Object o)
	{
		return registry.ensure(o.getClass(), supplier).add(o);
	}

	public static void registerAddress(final Address subject)
	{
		if(!register(subject))
		{
			return;
		}
	}

	public static void registerContact(final Contact subject)
	{
		if(!register(subject))
		{
			return;
		}
		register(subject.contactId());
		register(subject.note());
		registerAddress(subject.address());
	}

	public static void registerCorporation(final Corporation subject)
	{
		register(subject.contactId());

	}

	public static void countEntities(final ClientCorporation cc)
	{





//		int countInteger      = 0;
//		int countDouble       = 0;
//		int countString       = 0;
//
//		final int countEqHashTable  = 0;
//		int countHashTable    = 0;
//
//		int countBusinessYear = 0;
//		int countOrder        = 0;
//		int countOrderItem    = 0;
//		int countCity         = 0;
//		int countStreet       = 0;
//		int countVendor       = 0;
//
//
//		registry.ensure
//
//		countEqHashTable++;
//		for(final KeyValue<Integer, BusinessYear> bYears : cc.businessYears())
//		{
//			countInteger++; // key
//			countBusinessYear++; // value
//			countEqHashTable++; // orders
//			for(final KeyValue<String, Order> order : bYears.value().orders())
//			{
//				countString++; // key
//				countOrder++; // value
//				countHashTable++; // items
//				for(final KeyValue<Product, OrderItem> items : order.value().items())
//				{
//					// key not counted, gets counted via vendor iteration
//					countOrderItem++;
//					countInteger++; // amount
//					countDouble++; // total price
//				}
//			}
//		}
//
//		countEqHashTable++;
//		for(final KeyValue<String, City> city : cc.citiesByName())
//		{
//			countString++; // key
//			countCity++; // value
//			countEqHashTable++; // streets table
//
//			for(final KeyValue<String, Street> street : city.value().streetsByName())
//			{
//				countString++; // key
//				countStreet++; // value
//				countEqHashTable++; // residents table
//				// residents themselves are counted via customers/vendors iteration
//			}
//		}
//
//		countEqHashTable++;
//		for(final KeyValue<String, Vendor> vendor : cc.vendorsById())
//		{
//			countString++; // key
//			countVendor++; // value
//			countEqHashTable++; // products table
//
//			final Corporation corp = vendor.value().corporation();
//
//			for(final KeyValue<String, Product> product : vendor.value().productsByName())
//			{
//
//			}
//
//		}

	}

}

final class Generator
{
	private final int totalAmount;

//	private final EMailAddress[]      allEMailAddress     ; // exclusive to CallAddress
//	private final PhoneNumber[]       allPhoneNumber      ; // exclusive to CallAddress
	private final PostalAddress[]     allPostalAddress    ;
//	private final CallAddress[]       allCallAddress      ; // exclusive to Address
	private final Street[]            allStreet           ;
	private final City[]              allCity             ;
//	private final Address[]           allAddress          ; // exclusive to a Contact
	private final Vendor[]            allVendor           ;
	private final Product[]           allProduct          ;
	private final Person[]            allPerson           ;
	private final Corporation[]       allCorporation      ;
	private final PrivateCustomer[]   allPrivateCustomer  ;
	private final CorporateCustomer[] allCorporateCustomer;
	private final Order[]             allOrder            ;
	private final OrderItem[]         allOrderItem        ;
	private final BusinessYear[]      allBusinessYear     ;
	private final ClientCorporation[] allClientCorporation;

	private int usedPostalAddress = 0;
	private int usedPerson        = 0;
	private int usedCorporation   = 0;
	private int usedVendor        = 0;
	private int usedOrderItem     = 0;

	private final Customer[]          allCustomer         ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	Generator(final int totalAmount)
	{
		super();
		this.totalAmount = totalAmount;
//		this.allEMailAddress      = new EMailAddress     [Test.AMOUNT_EMailAddress     .calculateAmount(this.totalAmount)];
//		this.allPhoneNumber       = new PhoneNumber      [Test.AMOUNT_PhoneNumber      .calculateAmount(this.totalAmount)];
		this.allPostalAddress     = new PostalAddress    [Test.AMOUNT_PostalAddress    .calculateAmount(this.totalAmount)];
//		this.allCallAddress       = new CallAddress      [Test.AMOUNT_CallAddress      .calculateAmount(this.totalAmount)];
		this.allStreet            = new Street           [Test.AMOUNT_Street           .calculateAmount(this.totalAmount)];
		this.allCity              = new City             [Test.AMOUNT_City             .calculateAmount(this.totalAmount)];
//		this.allAddress           = new Address          [Test.AMOUNT_Address          .calculateAmount(this.totalAmount)];
		this.allVendor            = new Vendor           [Test.AMOUNT_Vendor           .calculateAmount(this.totalAmount)];
		this.allProduct           = new Product          [Test.AMOUNT_Product          .calculateAmount(this.totalAmount)];
		this.allPerson            = new Person           [Test.AMOUNT_Person           .calculateAmount(this.totalAmount)];
		this.allCorporation       = new Corporation      [Test.AMOUNT_Corporation      .calculateAmount(this.totalAmount)];
		this.allPrivateCustomer   = new PrivateCustomer  [Test.AMOUNT_PrivateCustomer  .calculateAmount(this.totalAmount)];
		this.allCorporateCustomer = new CorporateCustomer[Test.AMOUNT_CorporateCustomer.calculateAmount(this.totalAmount)];
		this.allOrder             = new Order            [Test.AMOUNT_Order            .calculateAmount(this.totalAmount)];
		this.allOrderItem         = new OrderItem        [Test.AMOUNT_OrderItem        .calculateAmount(this.totalAmount)];
		this.allBusinessYear      = new BusinessYear     [Test.AMOUNT_BusinessYear     .calculateAmount(this.totalAmount)];
		this.allClientCorporation = new ClientCorporation[1]; // may only be one for simple example

		this.allCustomer = new Customer[this.allPrivateCustomer.length + this.allCorporateCustomer.length];
	}

	final String infoGenerationAmounts()
	{
		final long amountAddressEntities = this.allPerson.length + this.allCorporation.length + this.allClientCorporation.length;

		final long totalCount = 0
			+this.allPostalAddress.length
			+this.allPostalAddress.length
			+this.allPostalAddress.length
			+amountAddressEntities
			+this.allStreet           .length
			+this.allCity             .length
			+amountAddressEntities
			+this.allVendor           .length
			+this.allProduct          .length
			+this.allPerson           .length
			+this.allCorporation      .length
			+this.allPrivateCustomer  .length
			+this.allCorporateCustomer.length
			+this.allOrder            .length
			+this.allOrderItem        .length
			+this.allBusinessYear     .length
			+this.allClientCorporation.length
		;

		final String infoString = "total amount = "+this.totalAmount+"\n"+
			"amount EMailAddress      = "+this.allPostalAddress.length    +"\n"+
			"amount PhoneNumber       = "+this.allPostalAddress.length    +"\n"+
			"amount PostalAddress     = "+this.allPostalAddress.length    +"\n"+
			"amount CallAddress       = "+amountAddressEntities         +"\n"+
			"amount Street            = "+this.allStreet           .length+"\n"+
			"amount City              = "+this.allCity             .length+"\n"+
			"amount Address           = "+amountAddressEntities           +"\n"+
			"amount Vendor            = "+this.allVendor           .length+"\n"+
			"amount Product           = "+this.allProduct          .length+"\n"+
			"amount Person            = "+this.allPerson           .length+"\n"+
			"amount Corporation       = "+this.allCorporation      .length+"\n"+
			"amount PrivateCustomer   = "+this.allPrivateCustomer  .length+"\n"+
			"amount CorporateCustomer = "+this.allCorporateCustomer.length+"\n"+
			"amount Order             = "+this.allOrder            .length+"\n"+
			"amount OrderItem         = "+this.allOrderItem        .length+"\n"+
			"amount BusinessYear      = "+this.allBusinessYear     .length+"\n"+
			"amount ClientCorporation = "+this.allClientCorporation.length+"\n"+
			"---\n"+
			"total generated = "+totalCount
		;

		return infoString;
	}

	static double generatePrice()
	{
		// price range [0.99; 9,999.99]
		return XMath.round2(XMath.random(10000) + 0.99);
	}

	static int randomOrderItemAmount()
	{
		return 1 + XMath.random(20);
	}


	static <T> T random(final T[] elements)
	{
		return elements[XMath.random(elements.length)];
	}

	final PostalAddress supplyPostAddress()
	{
		// ensure that every postal address is used at least once, after that return a random one
		return notNull(this.usedPostalAddress >= this.allPostalAddress.length
			? random(this.allPostalAddress)
			: this.allPostalAddress[this.usedPostalAddress++]
		);
	}


	final Person supplyPerson()
	{
		// ensure that every postal address is used at least once, after that return a random one
		return notNull(this.usedPerson >= this.allPerson.length
			? random(this.allPerson)
			: this.allPerson[this.usedPerson++]
		);
	}


	final Address createAddress(final Contact contact, final String number)
	{
		// exclusive call address (e-mail is unique per contact), pooled post address (multiple contacts in one address)
		return Test.Address(
			notNull(contact),
			notNull(this.supplyPostAddress()),
			Test.CallAddress(number)
		);
	}


	final Corporation supplyCorporation()
	{
		// ensure that every postal address is used at least once, after that return a random one
		return notNull(this.usedCorporation >= this.allCorporation.length
			? random(this.allCorporation)
			: this.allCorporation[this.usedCorporation++]
		);
	}


	final Vendor supplyVendor()
	{
		// ensure that every postal address is used at least once, after that return a random one
		return notNull(this.usedVendor >= this.allVendor.length
			? random(this.allVendor)
			: this.allVendor[this.usedVendor++]
		);
	}


	final OrderItem supplyOrderItem()
	{
		// ensure that every postal address is used at least once, after that return a random one
		if(this.usedOrderItem >= this.allOrderItem.length)
		{
			throw new RuntimeException("No more order items!");
		}
		return notNull(this.allOrderItem[this.usedOrderItem++]);
	}

	final void populateCity()
	{
		final City[] allCity = this.allCity;
		for(int i = 0; i < allCity.length; i++)
		{
			allCity[i] = Test.City(XChars.String(i));
		}
	}

	final void populateStreet()
	{
		final City[]   allCity   = this.allCity  ;
		final Street[] allStreet = this.allStreet;
		for(int i = 0; i < allStreet.length; i++)
		{
			final City city = random(allCity);
			final Street street = allStreet[i] = Test.Street(city, XChars.String(i));
			city.registerStreet(street);
		}
	}

	final void populatePostalAddress()
	{
		final Street[]        allStreet        = this.allStreet       ;
		final PostalAddress[] allPostalAddress = this.allPostalAddress;

		for(int i = 0; i < allPostalAddress.length; i++)
		{
			final Street street = random(allStreet);
			allPostalAddress[i] = Test.PostalAddress(street, XChars.String(i));
		}
	}

	final void populateVendor()
	{
		final XTable<String, Vendor> vendorsById = this.allClientCorporation[0].vendorsById();

		final Vendor[] allVendor = this.allVendor;

		for(int i = 0; i < allVendor.length; i++)
		{
			final Vendor vendor = Test.Vendor(this.supplyCorporation());
			allVendor[i] = vendor;
			vendorsById.add(vendor.corporation().contactId(), vendor);
		}
	}

	final void populateProduct()
	{
		final Product[] allProduct = this.allProduct;

		for(int i = 0; i < allProduct.length; i++)
		{
			final String number = XChars.String(i);
			final Vendor vendor = this.supplyVendor();
			final Product product = allProduct[i] = Test.Product("product "+number, vendor, generatePrice());
			vendor.registerProduct(product);
		}
	}

	final void populatePerson()
	{
		final Person[] allPerson = this.allPerson;

		for(int i = 0; i < allPerson.length; i++)
		{
			final String number = XChars.String(i);
			final Person person = allPerson[i] = Test.Person('p'+number, "firstname"+number,"lastname"+number, null);
			person.setAddress(this.createAddress(person, number));
			person.address().postalAddress().street().registerContact(person);
		}
	}

	final void populateCorporation()
	{
		final Corporation[] allCorporation = this.allCorporation;

		for(int i = 0; i < allCorporation.length; i++)
		{
			final String number = XChars.String(i);
			final Corporation corporation = allCorporation[i] = Test.Corporation(
				'c'+number,
				"corp-"+number,
				"tax_id-"+number,
				this.supplyPerson(),
				null
			);
			corporation.setAddress(this.createAddress(corporation, number));
			corporation.address().postalAddress().street().registerContact(corporation);
		}
	}

	final void populateCustomers()
	{
		final XTable<String, Customer> customersById = this.allClientCorporation[0].customersById();

		final PrivateCustomer[] allPrivateCustomer = this.allPrivateCustomer;
		for(int i = 0; i < allPrivateCustomer.length; i++)
		{
			final PrivateCustomer c = allPrivateCustomer[i] = Test.PrivateCustomer(this.supplyPerson());
			c.setBillingAddress(c.address());
			c.shippingAddresses().add(c.address());
			customersById.add(c.contactId(), c);
		}

		final CorporateCustomer[] allCorporateCustomer = this.allCorporateCustomer;
		for(int i = 0; i < allCorporateCustomer.length; i++)
		{
			final CorporateCustomer c = allCorporateCustomer[i] = Test.CorporateCustomer(this.supplyCorporation());
			c.setBillingAddress(c.address());
			c.shippingAddresses().add(c.address());
			customersById.add(c.contactId(), c);
		}

		System.arraycopy(allPrivateCustomer, 0, this.allCustomer, 0, allPrivateCustomer.length);
		System.arraycopy(allCorporateCustomer, 0, this.allCustomer, allPrivateCustomer.length, allCorporateCustomer.length);
	}

	final void populateOrder()
	{
		final Order[]    allOrder    = this.allOrder   ;
		final Customer[] allCustomer = this.allCustomer;

		for(int i = 0; i < allOrder.length; i++)
		{
			final String number = XChars.String(i);
			final Customer customer = random(allCustomer);

			final OrderItem initialOrderItem = this.supplyOrderItem();

			allOrder[i] = Test.Order(
				"order"+number,
				customer,
				HashTable.New(X.KeyValue(initialOrderItem.product(), initialOrderItem)),
				notNull(customer.billingAddress()),
				notNull(customer.shippingAddresses().get())
			);
		}

		int i = this.usedOrderItem;
		final OrderItem[] allOrderItem       = this.allOrderItem  ;
		final int         allOrderItemLength = allOrderItem.length;

		while(i < allOrderItemLength)
		{
			final OrderItem additionalOrderItem = allOrderItem[i++];

			// every order can contain any product only once. Collisions must therefore cause a retry.
			while(!random(allOrder).items().add(additionalOrderItem.product(), additionalOrderItem))
			{
				// retry
			}
		}

		// assign orders to business year
		final BusinessYear[] allBusinessYear = this.allBusinessYear;
		for(final Order order : allOrder)
		{
			random(allBusinessYear).orders().add(order.orderId(), order);
		}
	}

	final void populateOrderItem()
	{
		final OrderItem[] allOrderItem = this.allOrderItem;
		final Product[]   allProduct   = this.allProduct  ;
		for(int i = 0; i < allOrderItem.length; i++)
		{
			final Product product = random(allProduct);
			final int amount = randomOrderItemAmount();
			final double totalPrice = XMath.round2(amount * product.price());
			allOrderItem[i] = Test.OrderItem(product, amount, totalPrice);
		}
	}

	final void populateClientCorporation()
	{
		final ClientCorporation[] allClientCorporation = this.allClientCorporation;

		final ClientCorporation corporation = allClientCorporation[0] = Test.ClientCorporation(
			"client0",
			"ClientCorp",
			"tax_id-000",
			this.supplyPerson(),
			null
		);
		corporation.setAddress(this.createAddress(corporation, "CliCo"));
		corporation.address().postalAddress().street().registerContact(corporation);

		final LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		final int currentYear = ldt.getYear();

		final BusinessYear[] allBusinessYear    = this.allBusinessYear  ;
		final int            businessYearAmount = allBusinessYear.length;

		for(int y = 0; y < businessYearAmount; y++)
		{
			final Integer year = currentYear - businessYearAmount + y + 1;
			final BusinessYear by = Test.BusinessYear(corporation, year);
			allBusinessYear[y] = by;
			corporation.businessYears().add(year, by);
		}

		for(final City city : this.allCity)
		{
			corporation.citiesByName().add(city.name(), city);
		}
	}



	public ClientCorporation generateModelData()
	{
		this.populateCity();
		this.populateStreet();
		this.populatePostalAddress();

		this.populatePerson();
		this.populateCorporation();

		this.populateClientCorporation();

		this.populateCustomers();

		this.populateVendor();
		this.populateProduct();

		this.populateOrderItem();
		this.populateOrder();

		return this.allClientCorporation[0];
	}

}


final class GenerationAmount
{
	double fraction     ;
	int    minimumAmount;

	GenerationAmount(final double fraction, final int minimumAmount)
	{
		super();
		this.fraction = fraction;
		this.minimumAmount = minimumAmount;
	}

	final int calculateAmount(final int totalEntityCount)
	{
		return Math.max((int)(totalEntityCount * this.fraction / 100.0), this.minimumAmount);
	}

}
