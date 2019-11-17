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
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.mrikso.foldercompare.R;

import java.util.Date;

public class DateTime extends AppCompatActivity
{
	private long time;
	private Intent intent = new Intent();
	private DatePicker datePicker;
	private TimePicker timePicker;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.date_time);
        
        Intent i = getIntent();
        time = i.getLongExtra("time", -1);
        
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        
        if ( time != -1 )
        {
        	Date d = new Date( time );
        	datePicker.updateDate( d.getYear() + 1900, d.getMonth(), d.getDay() );
        	timePicker.setCurrentHour( d.getHours() );
        	timePicker.setCurrentMinute( d.getMinutes() );
        }
        
        Button setBtn = findViewById(R.id.filter_set);
        Button resetBtn = findViewById(R.id.filter_reset);
        Button cancelBtn = findViewById(R.id.filter_cancel);
        
        setBtn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Date d = new Date();
				
				d.setYear( datePicker.getYear() - 1900 );
				d.setMonth( datePicker.getMonth() );
				d.setDate( datePicker.getDayOfMonth() );
				d.setHours( timePicker.getCurrentHour() );
				d.setMinutes( timePicker.getCurrentMinute() );
				d.setSeconds( 0 );
				
				time = d.getTime();
				intent.putExtra("time", time);
				
				finish();
			}
		});
        
        resetBtn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				time = -1;
				intent.putExtra("time", time);
			}
		});
        
        cancelBtn.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				intent.putExtra("time", time);
				finish();
			}
		});
    }
    
	@Override
	protected void onResume()
	{
		super.onResume();
		intent.putExtra("time", time);
		setResult(RESULT_CANCELED, intent);
	}
}
