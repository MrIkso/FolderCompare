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

package com.mrikso.foldercompare.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.mrikso.foldercompare.R;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class LicenseViewActivity extends BaseActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.license);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
        InputStream input = getResources().openRawResource( R.raw.license );
		try
		{
			byte[] fileData = new byte[ input.available() ];
			input.read( fileData );
			input.close();
			GetListView().setText( new String( fileData, StandardCharsets.UTF_8) );
		}
		catch( Exception e )
		{
			GetListView().setText(e.getMessage());
			e.printStackTrace();
		}
    }
    
	private TextView GetListView()
	{
		return (TextView)findViewById( R.id.license_text );
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home: {
				finish();
			}
		}
		return super.onOptionsItemSelected(item);
	}
}
