package one.microstream.util;

/*-
 * #%L
 * microstream-base
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

import static one.microstream.chars.XChars.notEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import one.microstream.chars.XChars;

/**
 * 
 * @since 05.00.00
 *
 */
public interface BundleInfo
{
	public String name();
	
	public Version version();
	
	public String vendor();
	
	public String builtBy();
	
	public Instant builtAt();
	
	
	
	public static BundleInfo LoadBase()
	{
		return Load("microstream-base");
	}
	
	/**
	 * 
	 * @param bundleName the name of the bundle, in Maven environments the artifact id
	 * @return the loaded bundle info
	 */
	public static BundleInfo Load(final String bundleName)
	{
		return new Provider.ManifestAndMavenBased(
			notEmpty(bundleName)
		)
		.provideBundleInfo();
	}
	
	
	
	public static class Default implements BundleInfo
	{
		private final String  name   ;
		private final Version version;
		private final String  vendor ;
		private final String  builtBy;
		private final Instant builtAt;
		
		Default(
			final String  name   ,
			final Version version,
			final String  vendor ,
			final String  builtBy,
			final Instant builtAt
		)
		{
			super();
			this.name    = name   ;
			this.version = version;
			this.vendor  = vendor ;
			this.builtBy = builtBy;
			this.builtAt = builtAt;
		}

		@Override
		public String name()
		{
			return this.name;
		}

		@Override
		public Version version()
		{
			return this.version;
		}

		@Override
		public String vendor()
		{
			return this.vendor;
		}

		@Override
		public String builtBy()
		{
			return this.builtBy;
		}
		
		@Override
		public Instant builtAt()
		{
			return this.builtAt;
		}

		@Override
		public String toString()
		{
			return "BundleInfo"
				+ " [name=" + this.name
				+ ", version=" + this.version
				+ ", vendor=" + this.vendor
				+ ", builtBy=" + this.builtBy
				+ ", builtAt=" + this.builtAt
				+ "]";
		}
				
	}
	
	
	public interface Version
	{
		public String value();
		
		public Integer majorVersion();
		
		public Integer minorVersion();
		
		public Integer incrementalVersion();
		
		public Integer buildNumber();
		
		public String qualifier();
		
		public default boolean isSnapshot()
		{
			return this.value().endsWith("-SNAPSHOT");
		}
		
		
		public static class Default implements Version
		{
			private final String  value             ;
			private final Integer majorVersion      ;
			private final Integer minorVersion      ;
			private final Integer incrementalVersion;
			private final Integer buildNumber       ;
			private final String  qualifier         ;
			
			public Default(
				final String  value             ,
				final Integer majorVersion      ,
				final Integer minorVersion      ,
				final Integer incrementalVersion,
				final Integer buildNumber       ,
				final String  qualifier
			)
			{
				super();
				this.value              = value             ;
				this.majorVersion       = majorVersion      ;
				this.minorVersion       = minorVersion      ;
				this.incrementalVersion = incrementalVersion;
				this.buildNumber        = buildNumber       ;
				this.qualifier          = qualifier         ;
			}

			@Override
			public String value()
			{
				return this.value;
			}
			
			@Override
			public Integer majorVersion()
			{
				return this.majorVersion;
			}

			@Override
			public Integer minorVersion()
			{
				return this.minorVersion;
			}

			@Override
			public Integer incrementalVersion()
			{
				return this.incrementalVersion;
			}

			@Override
			public Integer buildNumber()
			{
				return this.buildNumber;
			}

			@Override
			public String qualifier()
			{
				return this.qualifier;
			}
			
			@Override
			public String toString()
			{
				return this.value;
			}
			
		}
		
		
		public static interface Parser
		{
			public Version parse(String value);
			
			
			public static class MavenVersionParser implements Parser
			{
				MavenVersionParser()
				{
					super();
				}
				
				@Override
				public Version parse(final String value)
				{
					Integer majorVersion       = null;
					Integer minorVersion       = null;
					Integer incrementalVersion = null;
					Integer buildNumber        = null;
					String  qualifier          = null;
					
					String  part1;
					String  part2              = null;

					final int dashIndex;
					if((dashIndex = value.indexOf('-')) < 0)
					{
						part1 = value;
					}
					else
					{
						part1 = value.substring(0, dashIndex);
						part2 = value.substring(dashIndex + 1);
					}
					
					if(part2 != null)
					{
						if(part2.length() == 1 || !part2.startsWith("0"))
						{
							buildNumber = tryParseInt(part2);
							if(buildNumber == null)
							{
								qualifier = part2;
							}
						}
						else
						{
							qualifier = part2;
						}
					}
					
					if(!part1.contains(".") && !part1.startsWith("0"))
					{
						majorVersion = tryParseInt(part1);
						if(majorVersion == null)
						{
							// qualifier is the whole version, including "-"
							qualifier   = value;
							buildNumber = null;
						}
					}
					else
					{
						boolean fallback = false;
						
						final StringTokenizer tokenizer = new StringTokenizer(part1, ".");
						if(tokenizer.hasMoreTokens())
						{
							majorVersion = getNextIntegerToken(tokenizer);
							if(majorVersion == null)
							{
								fallback = true;
							}
						}
						else
						{
							fallback = true;
						}
						if(tokenizer.hasMoreTokens())
						{
							minorVersion = getNextIntegerToken(tokenizer);
							if(minorVersion == null)
							{
								fallback = true;
							}
						}
						if(tokenizer.hasMoreTokens())
						{
							incrementalVersion = getNextIntegerToken(tokenizer);
							if(incrementalVersion == null)
							{
								fallback = true;
							}
						}
						if(tokenizer.hasMoreTokens())
						{
							qualifier = tokenizer.nextToken();
							fallback  = isDigits(qualifier);
						}
						
						// string tokenizer won't detect these and ignores them
						if(part1.contains("..") || part1.startsWith(".") || part1.endsWith("."))
						{
							fallback = true;
						}
						
						if(fallback)
						{
							// qualifier is the whole version, including "-"
							qualifier          = value;
							majorVersion       = null;
							minorVersion       = null;
							incrementalVersion = null;
							buildNumber        = null;
						}
					}
					
					return new Version.Default(
						value             ,
						majorVersion      ,
						minorVersion      ,
						incrementalVersion,
						buildNumber       ,
						qualifier
					);
				}
				
				private static Integer getNextIntegerToken(final StringTokenizer tok)
				{
					return tryParseInt(tok.nextToken());
				}
				
				private static Integer tryParseInt(final String s)
				{
					// for performance, check digits instead of relying later on catching NumberFormatException
					if(!isDigits(s))
					{
						return null;
					}
					
					try
					{
						final long longValue = Long.parseLong(s);
						if(longValue > Integer.MAX_VALUE)
						{
							return null;
						}
						return (int)longValue;
					}
					catch(final NumberFormatException e)
					{
						return null;
					}
				}
				
				private static boolean isDigits(final CharSequence cs)
				{
					if(XChars.isEmpty(cs))
					{
						return false;
					}
					final int length = cs.length();
					for(int i = 0; i < length; i++)
					{
						if(!Character.isDigit(cs.charAt(i)))
						{
							return false;
						}
					}
					return true;
				}
				
			}
			
		}
		
	}
	
	
	public static interface Provider
	{
		public BundleInfo provideBundleInfo();
		
		
		public static class ManifestAndMavenBased implements Provider
		{
			private final String bundleName;
			
			ManifestAndMavenBased(final String bundleName)
			{
				super();
				this.bundleName = bundleName;
			}

			@Override
			public BundleInfo provideBundleInfo()
			{
				try
				{
					final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
						.getResources("META-INF/MANIFEST.MF")
					;
					while(resources.hasMoreElements())
					{
						final Manifest   manifest   = this.loadManifest(resources.nextElement());
						final Attributes attributes = manifest.getMainAttributes();
						final String     name = attributes.getValue("Bundle-Name");
						if(this.bundleName.equals(name))
						{
							final Properties pomProperties = this.loadPomProperties();
							
							final Version version = new Version.Parser.MavenVersionParser().parse(
								pomProperties.getProperty("version")
							);
							final String  vendor  = attributes.getValue("Bundle-Vendor");
							final String  builtBy = attributes.getValue("built-by");
							final Instant builtAt = Instant.ofEpochMilli(
								Long.parseLong(attributes.getValue("Bnd-LastModified"))
							);
							return new BundleInfo.Default(
								name   ,
								version,
								vendor ,
								builtBy,
								builtAt
							);
						}
					}
					
					final Properties pomProperties = this.loadPomProperties();
					final String versionString = pomProperties.getProperty("version");
					if(versionString != null)
					{
						final Version version = new Version.Parser.MavenVersionParser().parse(versionString);
						return new BundleInfo.Default(
							this.bundleName,
							version        ,
							"MicroStream"  ,
							"MicroStream"  ,
							Instant.now()
						);
					}
				}
				catch(final Exception e)
				{
					throw new RuntimeException(e);
				}
				
				return null;
			}
			
			private Manifest loadManifest(final URL url) throws IOException
			{
				try(InputStream inputStream = url.openStream())
				{
					return new Manifest(inputStream);
				}
			}

			private Properties loadPomProperties() throws IOException
			{
				final URL pomPropertiesUrl = Thread.currentThread().getContextClassLoader().getResource(
					"META-INF/maven/one.microstream/" + this.bundleName + "/pom.properties"
				);
				final Properties pomProperties = new Properties();
				try(Reader pomReader = new InputStreamReader(
					pomPropertiesUrl.openStream(),
					StandardCharsets.UTF_8
				))
				{
					pomProperties.load(pomReader);
				}
				return pomProperties;
			}
			
		}
		
	}
	
}
