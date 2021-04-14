module microstream.afs.oraclecloud.objectstorage
{
	exports one.microstream.afs.oraclecloud.objectstorage.types;
	
	requires microstream.afs;
	requires microstream.afs.blobstore;
	requires microstream.base;
	requires microstream.configuration;
	requires nimbus.jose.jwt;
	requires oci.java.sdk.common;
	requires oci.java.sdk.objectstorage.extensions;
	requires oci.java.sdk.objectstorage.generated;
}
