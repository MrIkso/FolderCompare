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

public class TaskCompletionHandler
{
	public void OnComplete()
	{
		taskCompleted = true;
		try
		{
			synchronized( waitObject )
			{
				waitObject.wait();
			}
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
	
	public boolean IsTaskCompleted()
	{
		return taskCompleted;
	}
	
	public void CompletionAccepted()
	{
		synchronized( waitObject )
		{
			waitObject.notify();
		}
	}
	
	private boolean taskCompleted = false;
	private Object waitObject = new Object();
}
