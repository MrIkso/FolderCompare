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
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mrikso.foldercompare.R;

public class About extends AppCompatActivity implements OnClickListener
{
	private static final String[] EMAIL = {"folder.compare@gmail.com"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);
		
		String version = "";
		try
		{
			version = getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName;
		}
		catch( NameNotFoundException e )
		{
			e.printStackTrace();
		}
		
		String text = "Folder Compare v" + version + ": If you have any questions or "
						+"comments, please email developers."
						+"\n\nThank you\n";
		
		TextView label = (TextView)findViewById(R.id.about_top_label);
		label.setText(text);
		
		Button email = (Button)findViewById(R.id.about_email);
		email.setOnClickListener(this);
		
		Button license = (Button)findViewById(R.id.about_license);
		license.setOnClickListener(this);
	}

	@Override
	public void onClick(View view)
	{
		int id = view.getId();
		Intent i = new Intent();
		
		switch(id) {
			case R.id.about_email:
				i.setAction(android.content.Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL, EMAIL);
				try {
					startActivity(Intent.createChooser(i, "Email using..."));
					
				} catch(ActivityNotFoundException e) {
					Toast.makeText(this, "Sorry, could not start the email", Toast.LENGTH_SHORT).show();
				}
				break;
			
			case R.id.about_license:
				Intent intent = new Intent( this, LicenseView.class );
				startActivity( intent );
				break;
		}
	}
}
