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

package com.mrikso.foldercompare.comparator;

import com.mrikso.foldercompare.FileComparator.CompareInfo;

import java.io.File;


public class CompareItem
{
	private CompareInfo cmpInfo;
	private boolean isEmpty;
	
	public CompareItem( CompareInfo cmpInfo )
	{
		if ( cmpInfo == null )
		{
			isEmpty = true;
			return;
		}
		
		this.cmpInfo = cmpInfo;
	}
	
	public CompareItem( String fileName )
	{
		cmpInfo = new CompareInfo();
		cmpInfo.SetFile( new File( fileName ) );
	}
	
	public boolean IsCompared()
	{
		if ( isEmpty )
			return false;
		
		return cmpInfo.IsCompared();
	}
	
	public boolean IsUnique()
	{
		if ( isEmpty )
			return false;
		
		return cmpInfo.IsUnique();
	}
	
	public boolean IsEqualToOpposite()
	{
		if ( isEmpty )
			return false;
		
		return cmpInfo.IsEqualToOpposite();
	}
	
	public File GetFile()
	{
		return cmpInfo.GetFile();
	}
	
	public String GetFileName()
	{
		if ( isEmpty )
			return "";
		
		return cmpInfo.GetFile().getName();
	}
	
	public String GetFilePath()
	{
		if ( isEmpty )
			return "";
		
		return cmpInfo.GetFile().getAbsolutePath();
	}
	
	public void SetEmpty( boolean empty )
	{
		isEmpty = empty;
	}
	
	public boolean IsEmpty()
	{
		return isEmpty;
	}
	
	public static CompareItem GetEmptyItem()
	{
		return emptyItem;
	}
	
	private static final CompareItem emptyItem = new CompareItem( (CompareInfo)null );
}
