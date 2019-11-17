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

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

import com.mrikso.foldercompare.R;
import com.mrikso.foldercompare.comparator.CompareConfig;

public class CompareSettings extends AppCompatActivity
{
	private CompareConfig cmpConfig = new CompareConfig();
	private Intent intent = new Intent();
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings_cmp);
        
        Intent i = getIntent();
        cmpConfig.SetShowOnlyEq( i.getExtras().getBoolean( "only_eq" ) );
        cmpConfig.SetShowFilesNotExistsOnly( i.getExtras().getBoolean( "unique" ) );
        cmpConfig.SetShowOnlyCompared( i.getExtras().getBoolean( "compared" ) );
        cmpConfig.SetShowHidden( i.getExtras().getBoolean( "hidden" ) );
        
		CheckBox onlyEq_bx = findViewById(R.id.setting_only_eq);
		CheckBox unique_bx = findViewById(R.id.setting_unique);
		CheckBox compared_bx = findViewById(R.id.setting_compared);
		CheckBox hidden_bx = findViewById(R.id.setting_hidden);
		
		onlyEq_bx.setChecked( cmpConfig.GetShowOnlyEq() );
		unique_bx.setChecked( cmpConfig.GetShowFilesNotExistsOnly() );
		compared_bx.setChecked( cmpConfig.GetShowOnlyCompared() );
		hidden_bx.setChecked( cmpConfig.GetShowHidden() );
		
		onlyEq_bx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cmpConfig.SetShowOnlyEq( isChecked );
				intent.putExtra("only_eq", isChecked);
			}
		});
		
		unique_bx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cmpConfig.SetShowFilesNotExistsOnly( isChecked );
				intent.putExtra("unique", isChecked);
			}
		});
		
		compared_bx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cmpConfig.SetShowOnlyCompared( isChecked );
				intent.putExtra("compared", isChecked);				
			}
		});
		
		hidden_bx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cmpConfig.SetShowHidden( isChecked );
				intent.putExtra("hidden", isChecked);				
			}
		});
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		intent.putExtra("only_eq", cmpConfig.GetShowOnlyEq());
		intent.putExtra("unique", cmpConfig.GetShowFilesNotExistsOnly());
		intent.putExtra("compared", cmpConfig.GetShowOnlyCompared());
		intent.putExtra("hidden", cmpConfig.GetShowHidden());
			
		setResult(RESULT_CANCELED, intent);
	}
}
