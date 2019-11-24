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

package com.mrikso.foldercompare.util;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class Utils
{
	public static final int EXTERNAL_READ_PERMISSION_GRANT = 0x1a;
	public static String GetFilePermissions( File file )
	{
		String p = "-";
		
		if ( file.isDirectory() )
			p += "d";
		if ( file.canRead() )
			p += "r";
		if ( file.canWrite() )
			p += "w";
		
		return p;
	}
	
	public static String ReadFileAsString( String path )
	{
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(new File(path));
			FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			if ( stream != null )
			{
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	public static void WriteStringToFile( String data, String filePath )
	{
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter( new FileWriter( filePath ) );
            writer.write( data );
        }
        catch( IOException e )
        {
        	e.printStackTrace();
        }
        finally
        {
            try
            {
                if ( writer != null)
                        writer.close();
            }
            catch( IOException e ) {}
        }
	}

	public static boolean checkStorageAccessPermissions(Context context) {   //Only for Android M and above.
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			String permission = "android.permission.READ_EXTERNAL_STORAGE";
			int res = context.checkCallingOrSelfPermission(permission);
			boolean isGranted = res == PackageManager.PERMISSION_GRANTED;
			if (!isGranted) {
				((AppCompatActivity) context).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_READ_PERMISSION_GRANT);
			}
			return isGranted;
		} else {   //Pre Marshmallow can rely on Manifest defined permissions.
			return true;
		}
	}
	public static boolean startActivity(@NonNull Intent intent, @NonNull Fragment fragment) {
		try {
			fragment.startActivity(intent);
			return true;
		} catch (ActivityNotFoundException | IllegalArgumentException e) {
			e.printStackTrace();
			//ToastUtils.show(R.string.activity_not_found, fragment.requireContext());
			return false;
		}
	}
}
