package one.microstream.afs.nio.types;

/*-
 * #%L
 * microstream-afs-nio
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AWritableFile;
import one.microstream.collections.XArrays;

public interface NioWritableFile extends NioReadableFile, AWritableFile
{
	public static NioWritableFile New(
        final AFile  actual,
        final Object user  ,
        final Path   path
    )
    {
        return NioWritableFile.New(actual, user, path, null);
    }
    
    public static NioWritableFile New(
    	final AFile       actual     ,
    	final Object      user       ,
    	final Path        path       ,
    	final FileChannel fileChannel
    )
    {
        return new NioWritableFile.Default<>(
            notNull(actual),
            notNull(user)  ,
            notNull(path)  ,
            mayNull(fileChannel)
        );
    }
    
    public class Default<U> extends NioReadableFile.Default<U> implements NioWritableFile
    {
    	///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
    	
        protected Default(
        	final AFile       actual     ,
        	final U           user       ,
        	final Path        path       ,
        	final FileChannel fileChannel
        )
        {
            super(actual, user, path, fileChannel);
        }
        
        
        
        ///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
        
        @Override
        protected void validateOpenOptions(final OpenOption... options)
        {
        	/*
        	 * override super class (readable) implementation to do no validation
        	 * since everything is allowed for writable files.
        	 */
        }
        
        @Override
        protected OpenOption[] normalizeOpenOptions(final OpenOption... options)
        {
        	// super class implementation ensures READ
    		final OpenOption[] superOptions = super.normalizeOpenOptions(options);
    		
    		// this implementation ensures WRITE
    		return XArrays.ensureContained(superOptions, StandardOpenOption.WRITE);
        }
                
    }
    
}
