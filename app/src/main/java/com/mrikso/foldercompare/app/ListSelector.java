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

package com.mrikso.foldercompare.app;

import android.graphics.Color;

class ListSelector
{
	private FileListView left, right;
	private FileListView selected;
	private boolean attemptExit;

	public void SetListView( FileListView left, FileListView right )
	{
		this.left = left;
		this.right = right;
	}
	
	private void Unselect( FileListView v )
	{
		v.GetDirView().setBackgroundColor(Color.TRANSPARENT);
	}
	
	private void Select( FileListView v )
	{
		v.GetDirView().setBackgroundColor(Color.parseColor("#9dccf9"));
	}
	
	public void SetAttemptExit( boolean attemptExit )
	{
		this.attemptExit = attemptExit;
	}
	
	public boolean GetAttemptExit()
	{
		return attemptExit;
	}
	
	public void SetView( FileListView v )
	{
		if ( selected != null )
			Unselect( selected );
		
		Select( v );
		
		attemptExit = false;
		selected = v;
	}
	
	public void SetView( int listType )
	{
		if ( listType == FileListView.LIST_LEFT )
		{
			SetView( left );
		}
		else
		if ( listType == FileListView.LIST_RIGHT )
		{
			SetView( right );
		}
	}
	
	public FileListView GetView()
	{
		return selected;
	}
	
	public int GetListType()
	{
		return selected.GetListType();
	}
}
