/*
===========================================================================

FolderCompare Source Code
Copyright (C) 2012 Andrey Budnik. 

This file is part of the FolderCompare Source Code.  

FolderCompare Source Code is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FolderCompare Source Code is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FolderCompare Source Code.  If not, see <http://www.gnu.org/licenses/>.

===========================================================================
*/

package com.mrikso.foldercompare.FileComparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

public class FileComparator
{
	public boolean Compare( String filePath1, String filePath2 )
	{
		File f1 = new File( filePath1 );
		File f2 = new File( filePath2 );
		
		return Compare( f1, f2 );
	}
	
	public boolean Compare( File f1, File f2 )
	{
		if ( f1.isDirectory() != f2.isDirectory() )
			return false;
		
		if ( caseSensitive )
		{
			if ( !f1.getName().equals( f2.getName() ) )
				return false;
		}
		else
		{
			if ( !f1.getName().equalsIgnoreCase( f2.getName() ) )
				return false;
		}
		
		if ( f1.isDirectory() )
		{
			if ( !traverseFolder )
				return true;
			
			return CompareDirs( f1, f2 );
		}
		
		return CompareFiles( f1, f2 );		
	}
	
	private boolean CompareFiles( File f1, File f2 )
	{
		if ( !f1.isFile() || !f2.isFile() )
			return false;
		
		if ( f1.length() != f2.length() )
			return false;
		
		if ( !compareContent )
			return true;
		
		return FileContentsEquals( f1, f2 );
	}
	
	private boolean CompareDirs( File f1, File f2 )
	{
		File files1[] = f1.listFiles();
		File files2[] = f2.listFiles();
		
		if ( files1 == null || files2 == null )
			return false;
		
		int size = files1.length;
		if ( size != files2.length )
			return false;
		
		boolean found;
		for( int i = 0; i < size; i++ )
		{
			found = false;
			
			for( int j = 0; j < size; j++ )
			{
				if ( isIterrupted )
					break;
				
				if ( Compare( files1[i], files2[j] ) )
				{
					found = true;
					break;
				}
			}
			
			if ( !found )
				return false;
		}
		
		return true;
	}

	private boolean InputStreamEquals( InputStream is1, InputStream is2 )
	{
		if(is1 == is2) return true;
		if(is1 == null && is2 == null) return true;
		if(is1 == null || is2 == null) return false;
		try 
		{
			byte buff1[] = new byte[ BUFFER_SIZE ];
			byte buff2[] = new byte[ BUFFER_SIZE ];
			int read1 = -1;
			int read2 = -1;

			do {
				int offset1 = 0;
				while (offset1 < BUFFER_SIZE
               				&& (read1 = is1.read(buff1, offset1, BUFFER_SIZE-offset1)) >= 0) {
            				offset1 += read1;
        			}

				int offset2 = 0;
				while (offset2 < BUFFER_SIZE
               				&& (read2 = is2.read(buff2, offset2, BUFFER_SIZE-offset2)) >= 0) {
            				offset2 += read2;
        			}
				if(offset1 != offset2) return false;
				if(offset1 != BUFFER_SIZE) {
					Arrays.fill(buff1, offset1, BUFFER_SIZE, (byte)0);
					Arrays.fill(buff2, offset2, BUFFER_SIZE, (byte)0);
				}
				if(!Arrays.equals(buff1, buff2)) return false;
			} while(read1 >= 0 && read2 >= 0);
			if(read1 < 0 && read2 < 0) return true;	// both at EOF
			return false;

		} catch (Exception ei) {
			return false;
		}
	}

	private boolean FileContentsEquals( File file1, File file2 )
	{
		InputStream is1 = null;
		InputStream is2 = null;
		if(file1.length() != file2.length()) return false;

		try {
			is1 = new FileInputStream(file1);
			is2 = new FileInputStream(file2);

			return InputStreamEquals(is1, is2);

		} catch (Exception ei) {
			return false;
		} finally {
			try {
				if(is1 != null) is1.close();
				if(is2 != null) is2.close();
			} catch (Exception ei2) {}
		}
	}
	
	public static void main( String[] args )
	{
		FileComparator c = new FileComparator();
		
		final String dir = System.getProperty("user.dir");
		
		long s = System.currentTimeMillis();
		
		assert c.Compare( dir + "/test_data/d1/f1.txt", dir + "/test_data/d2/f1.txt" );
		assert c.Compare( dir + "/test_data/d1/empty", dir + "/test_data/d2/empty" );
		//assert c.Compare( dir + "/test_data/d1/t.avi", dir + "/test_data/d2/t.avi" );
		
		System.out.println( System.currentTimeMillis() - s );
		System.out.println( "done" );
	}
	
	public boolean IsTraverseFolder()
	{
		return traverseFolder;
	}

	public void SetTraverseFolder( boolean traverseFolder )
	{
		this.traverseFolder = traverseFolder;
	}

	public boolean IsCompareContent()
	{
		return compareContent;
	}

	public void SetCompareContent( boolean compareContent )
	{
		this.compareContent = compareContent;
	}
	
	public void SetInterrupted( boolean isIterrupted )
	{
		this.isIterrupted = isIterrupted;
	}
	
	public boolean IsInterrupted()
	{
		return isIterrupted;
	}

	private static final int BUFFER_SIZE = 64 * 1024;
	private boolean compareContent = true;
	private boolean traverseFolder = true;
	private boolean caseSensitive = true;
	private boolean isIterrupted;
}
