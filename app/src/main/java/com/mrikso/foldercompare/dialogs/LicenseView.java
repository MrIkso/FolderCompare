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

package com.mrikso.foldercompare.dialogs;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mrikso.foldercompare.R;

import java.io.InputStream;

public class LicenseView extends AppCompatActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.license);
        InputStream input = getResources().openRawResource( R.raw.license );
		try
		{
			byte[] fileData = new byte[ input.available() ];
			input.read( fileData );
			input.close();
			GetListView().setText( new String( fileData, "UTF-8" ) );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
    }
    
	private TextView GetListView()
	{
		return (TextView)findViewById( R.id.license_text );
	}
}
