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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;


public class CompareTask implements Runnable
{
	private void ListFiles()
	{
		File dir;
		File[] files;
		int dirsCnt = 0;
		
		dir = new File( pathLeft );
		if ( dir.isDirectory() )
		{
			files = dir.listFiles();
			Arrays.sort( files );
			dirsCnt += files.length;
			synchronized( cmpInfoMutex )
			{
				for( File f : files )
				{
					CompareInfo cmpInfo = new CompareInfo();
					cmpInfo.SetFile( f );
					compareInfoLeft.add( cmpInfo );
				}
			}
		}
		
		dir = new File( pathRight );
		if ( dir.isDirectory() )
		{
			files = dir.listFiles();
			Arrays.sort( files );
			dirsCnt += files.length;
			synchronized( cmpInfoMutex )
			{
				for( File f : files )
				{
					CompareInfo cmpInfo = new CompareInfo();
					cmpInfo.SetFile( f );
					compareInfoRight.add( cmpInfo );
				}
			}
		}
		
		for( ProgressHandler handler : progressHandlers )
		{
			handler.SetDirsTotal( dirsCnt );
		}
	}
	
	private void Compare( CompareInfo cmpInfo, ArrayList< CompareInfo > cmpInfoList )
	{
		if ( cmpInfo.IsCompared() )
			return;
		
		String fileName = cmpInfo.GetFile().getName();
		
		for( CompareInfo opCmpInfo : cmpInfoList )
		{
			if ( comparator.IsInterrupted() )
				break;
			
			String opFileName = opCmpInfo.GetFile().getName();
			
			if ( fileName.equalsIgnoreCase( opFileName ) )
			{
				cmpInfo.SetUnique( false );
				opCmpInfo.SetUnique( false );
				
				boolean result = comparator.Compare( cmpInfo.GetFile(), opCmpInfo.GetFile() );
				cmpInfo.SetEqualToOpposite( result );
				opCmpInfo.SetEqualToOpposite( result );
				
				opCmpInfo.SetCompared( true );
				break;
			}
		}
		
		cmpInfo.SetCompared( true );
	}
	
	private void CompareFiles()
	{
		int i, j;
		
		ArrayList< CompareInfo > left = compareInfoLeft;
		ArrayList< CompareInfo > right = compareInfoRight;
		CompareInfo cmpInfo;
		
		boolean takeRight = false;
		boolean takenLeft;
		
		i = j = 0;
		
		while( !comparator.IsInterrupted() )
		{
			if ( ( i >= left.size() ) && ( j >= right.size() ) )
				break;
			
			if ( !takeRight )
			{
				if ( i < left.size() )
				{
					cmpInfo = left.get( i );
					takenLeft = true;
					i++;
				}
				else
				{
					cmpInfo = right.get( j );
					takenLeft = false;
					j++;
				}
			}
			else
			{
				if ( j < right.size() )
				{
					cmpInfo = right.get( j );
					takenLeft = false;
					j++;
				}
				else
				{
					cmpInfo = left.get( i );
					takenLeft = true;
					i++;
				}
			}
			
			takeRight = !takeRight;
			
			Compare( cmpInfo, takenLeft ? right : left );
			
			for( ProgressHandler handler : progressHandlers )
			{
				handler.SetDirsCompared( i + j );
			}
		}
	}
	
	private void ComputeStatistics()
	{
		for( CompareInfo cmpInfo : compareInfoLeft )
		{
			if ( cmpInfo.IsCompared() )
			{
				if ( cmpInfo.IsUnique() )
					statistics.setUniqueLeftCnt( statistics.getUniqueLeftCnt() + 1 );
				
				statistics.setTotalCompared( statistics.getTotalCompared() + 1 );
				
				if ( !cmpInfo.IsEqualToOpposite() )
					statistics.setChangedFilesCnt( statistics.getChangedFilesCnt() + 1 );
				else
					statistics.setUnchangedFilesCnt( statistics.getUnchangedFilesCnt() + 1 );
			}
		}
		
		for( CompareInfo cmpInfo : compareInfoRight )
		{
			if ( cmpInfo.IsCompared() )
			{
				if ( cmpInfo.IsUnique() )
					statistics.setUniqueRightCnt( statistics.getUniqueRightCnt() + 1 );
				
				statistics.setTotalCompared( statistics.getTotalCompared() + 1 );
				
				if ( !cmpInfo.IsEqualToOpposite() )
					statistics.setChangedFilesCnt( statistics.getChangedFilesCnt() + 1 );
				else
					statistics.setUnchangedFilesCnt( statistics.getUnchangedFilesCnt() + 1 );
			}
		}
	}
	
	private boolean IsInputDataValid()
	{
		return pathLeft != null && pathRight != null && statistics != null;
	}
	
	@Override
	public void run()
	{
		if ( !IsInputDataValid() )
			return;
		
		ListFiles();
		CompareFiles();
		ComputeStatistics();
		
		OnComplete();
	}
	
	private void OnComplete()
	{
		for( Object o : doneEvents )
		{
			synchronized( o )
			{
				o.notify();
			}
		}
		
		if ( !comparator.IsInterrupted() )
		{
			for( TaskCompletionHandler h : completionHandlers )
			{
				h.OnComplete();
			}
		}
	}
	
	public void Stop()
	{
		comparator.SetInterrupted( true );
	}
	
	public void SetPath( String pathLeft, String pathRight )
	{
		this.pathLeft = pathLeft;
		this.pathRight = pathRight;
	}
	
	public void SetCompareStatistics( CompareStatistics stat )
	{
		statistics = stat;
	}
	
	public void AddTaskCompletionHandler( TaskCompletionHandler handler )
	{
		completionHandlers.add( handler );
	}
	
	public void AddDoneEvent( Object doneEvent )
	{
		doneEvents.add( doneEvent );
	}
	
	public void AddProgressHandler( ProgressHandler handler )
	{
		progressHandlers.add( handler );
	}
	
	public void GetCompareInfo( ArrayList< CompareInfo > left, ArrayList< CompareInfo > right )
	{
		left.clear();
		right.clear();
		synchronized( cmpInfoMutex )
		{
			for( CompareInfo cmpInfo : compareInfoLeft )
			{
				try
				{
					left.add( cmpInfo.clone() );
				}
				catch( CloneNotSupportedException e )
				{
					e.printStackTrace();
				}
			}
			
			for( CompareInfo cmpInfo : compareInfoRight )
			{
				try
				{
					right.add( cmpInfo.clone() );
				}
				catch( CloneNotSupportedException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main( String[] args )
	{
		CompareTask c = new CompareTask();
		
		long s = System.currentTimeMillis();
		
		c.SetPath("C:/Python27", "C:/PythonOgre");
		//c.SetPath("C:/Windows/System32", "C:/Windows/System32");
		c.SetCompareStatistics( new CompareStatistics() );
		c.run();
		
		System.out.println( System.currentTimeMillis() - s );
		System.out.println( "done" );
	}
	
	private CompareStatistics statistics;
	
	private ArrayList< CompareInfo > compareInfoLeft = new ArrayList< CompareInfo >();
	private ArrayList< CompareInfo > compareInfoRight = new ArrayList< CompareInfo >();
	private Object cmpInfoMutex = new Object();
	
	private String pathLeft;
	private String pathRight;
	private FileComparator comparator = new FileComparator();
	
	private Vector< ProgressHandler > progressHandlers = new Vector< ProgressHandler >();
	
	private Vector< Object > doneEvents = new Vector< Object >();
	private Vector< TaskCompletionHandler > completionHandlers = new Vector< TaskCompletionHandler >();
}
