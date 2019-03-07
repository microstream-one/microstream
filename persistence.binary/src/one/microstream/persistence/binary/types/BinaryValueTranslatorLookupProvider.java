package one.microstream.persistence.binary.types;

import one.microstream.typing.TypeMappingLookup;

public interface BinaryValueTranslatorLookupProvider
{
	public TypeMappingLookup<BinaryValueSetter> mapping(boolean switchByteOrder);
}
