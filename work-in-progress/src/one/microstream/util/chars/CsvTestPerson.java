package one.microstream.util.chars;

import java.text.SimpleDateFormat;
import java.util.Date;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.time.XTime;
import one.microstream.util.xcsv.XCSV;
import one.microstream.util.xcsv.XCsvAssembler;
import one.microstream.util.xcsv.XCsvRowAssembler;

class CsvTestPerson
{
	final String firstName  ;
	final String lastName   ;
	final Date   dateOfBirth;
	final double weight     ;
	final double height     ;
	final String profile    ;


	public CsvTestPerson(
		final String firstName  ,
		final String lastName   ,
		final Date   dateOfBirth,
		final double weight     ,
		final double height     ,
		final String profile
	)
	{
		super();
		this.firstName   = firstName  ;
		this.lastName    = lastName   ;
		this.dateOfBirth = dateOfBirth;
		this.weight      = weight     ;
		this.height      = height     ;
		this.profile     = profile    ;
	}

	public static final void toCsvRow(final CsvTestPerson person, final XCsvAssembler rowAssembler)
	{
		rowAssembler.addRowValueDelimited(person.firstName);
		rowAssembler.addRowValueDelimited(person.lastName);
		rowAssembler.addRowValueDelimited(new SimpleDateFormat("yyyy-MM-dd").format(person.dateOfBirth));
		rowAssembler.addRowValueSimple   (Double.toString(person.weight));
		rowAssembler.addRowValueSimple   (Double.toString(person.height));
		rowAssembler.addRowValueDelimited(person.profile);
	}

//	public static final CsvVarStringEntityAssembler<Person> csvAssembler =
//		(final Person person, final CsvRowAssembler rowAssembler) ->
//		{
//			rowAssembler.addRowValueDelimited(person.firstName);
//			rowAssembler.addRowValueDelimited(person.lastName);
//			rowAssembler.addRowValueDelimited(DATE_FORMAT_DOB_CSV.format(person.dateOfBirth));
//			rowAssembler.addRowValueSimple   (Double.toString(person.weight));
//			rowAssembler.addRowValueSimple   (Double.toString(person.height));
//		}
//	;


	public static void main(final String[] args)
	{
		final VarString vs = VarString.New();
		final XCsvAssembler csvAssembler = XCSV.AssemblerBuilder()
			.setValueSeperatorPrefix("\t")
			.setValueSeperatorSuffix(" ")
			.buildRowAssembler(vs)
		;
		XCSV.assembleRow(csvAssembler, XCsvRowAssembler::addNonNullDelimited,
			X.List("First Name", "Last Name", "Date Of Birth", "Weight", "Height", "Profile")
		);
		XCSV.assembleRows(csvAssembler, CsvTestPerson::toCsvRow,
			X.List(
				new CsvTestPerson("Simon", "Simple", XTime.now(), 75.5, 182.8, null),
				new CsvTestPerson("Sophie", "Sophisticated Very", XTime.now(), 59.6, 169.9, "text with newlines\n & \"stuff\"")
			)
		);
		System.out.println(vs);
	}

}
