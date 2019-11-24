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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.mrikso.foldercompare.R;
import com.mrikso.foldercompare.comparator.FilterParams;
import com.mrikso.foldercompare.dialogs.DateTime;

public class FilterSettingsActivity extends BaseActivity
{
	private static final int TIME_FROM = 1;
	private static final int TIME_TO   = 2;
	
	private Intent intent = new Intent();
	private FilterParams fltParams = new FilterParams();

	private String[] spinnerSizes = {"Bytes", "KB", "MB", "GB"};
	
	private TextView sizeFromText, sizeToText;
	private TextView timeFromText, timeToText;
	private Spinner spinnerFrom, spinnerTo;
	private TextView editFilter;
	private CheckBox useFilter;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings_filter);
        
        Intent i = getIntent();
		fltParams.SetSize( i.getLongExtra("size_from", -1), i.getLongExtra("size_to", -1) );
		fltParams.SetTime( i.getLongExtra("time_from", -1), i.getLongExtra("time_to", -1) );
		fltParams.AddNames( i.getStringArrayListExtra( "names" ) );
		fltParams.SetUseFilter( i.getBooleanExtra("use_filter", false) );
		
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerSizes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
		spinnerFrom = findViewById(R.id.spinner_from_kb );
		spinnerFrom.setAdapter( adapter );
		spinnerFrom.setSelection( 1 );
		
		spinnerTo = findViewById(R.id.spinner_to_kb );
		spinnerTo.setAdapter( adapter );
		spinnerTo.setSelection( 1 );
		
		sizeFromText = findViewById(R.id.edit_size_from);
		if ( fltParams.GetSizeFrom() != -1 )
		{
			long[] measure = GetMeasure(fltParams.GetSizeFrom());
			sizeFromText.setText( String.valueOf( measure[0] ) );
			spinnerFrom.setSelection( (int)measure[1] );
		}
		
		sizeToText = findViewById(R.id.edit_size_to);
		if ( fltParams.GetSizeTo() != -1 )
		{
			long[] measure = GetMeasure(fltParams.GetSizeTo());
			sizeToText.setText( String.valueOf( measure[0] ) );
			spinnerTo.setSelection( (int)measure[1] );
		}
		
		timeFromText = findViewById(R.id.edit_date_from);
		if ( fltParams.GetTimeFrom() != -1 )
		{
			timeFromText.setText( TimeToStr( fltParams.GetTimeFrom() ) );
		}
		
		timeToText = findViewById(R.id.edit_date_to);
		if ( fltParams.GetTimeTo() != -1 )
		{
			timeToText.setText( TimeToStr( fltParams.GetTimeTo() ) );
		}

		timeFromText.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				Intent intent = new Intent( context, DateTime.class );
				intent.putExtra("time", fltParams.GetTimeFrom());
				((Activity)context).startActivityForResult( intent, TIME_FROM );
			}
		});
		
		timeToText.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = v.getContext();
				Intent intent = new Intent( context, DateTime.class );
				intent.putExtra("time", fltParams.GetTimeFrom());
				((Activity)context).startActivityForResult( intent, TIME_TO );
			}
		});
        
		editFilter = findViewById(R.id.edit_filter);
		
		if ( fltParams.GetNames().size() > 0 )
		{
			String filter = "";
			for( String name : fltParams.GetNames() )
			{
				filter += name + ";";
			}
			editFilter.setText( filter );
		}
		
		useFilter = findViewById(R.id.check_use_filter);
		useFilter.setChecked( fltParams.IsUseFilter() );
    }
    
    private long[] GetMeasure( long size )
    {
		long[] result = new long[2];
    	
    	if ( size == 0 )
    	{
    		result[0] = 0;
    		result[1] = 0;
    		return result;
    	}
    	
    	result[0] = size;
    	long bytes = 1024 * 1024 * 1024;
    	
    	for( int maxMeasure = 3; maxMeasure > 0; maxMeasure--, bytes /= 1024 )
    	{
	    	if ( size % bytes == 0 )
	    	{
	    		result[0] = size / bytes;
	    		result[1] = maxMeasure;
	    		return result;
	    	}
    	}
    	
    	return result;
    }
    
    private long GetSize( long units, long measure )
    {
    	long bytes = 1;
    	
    	for( int i = 0; i < measure; i++ )
    	{
    		bytes *= 1024;
    	}
    	
    	return units * bytes;
    }
    
    private String TimeToStr( long time )
    {
		Date date = new Date( time );
		return DateFormat.getDateTimeInstance().format( date );
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if ( resultCode == RESULT_CANCELED )
    	{
	    	if ( requestCode == TIME_FROM )
	    	{
	    		long time = data.getLongExtra("time", -1 );
	    		fltParams.SetTime( time, fltParams.GetTimeTo() );
	    		intent.putExtra("time_from", fltParams.GetTimeFrom());
	    		if ( time != -1 )
	    		{
	    			timeFromText.setText( TimeToStr( time ) );
	    		}
	    		else
	    			timeFromText.setText( "" );
	    	}
	    	else
	    	if ( requestCode == TIME_TO )
	    	{
	    		long time = data.getLongExtra("time", -1 );
	    		fltParams.SetTime( fltParams.GetTimeFrom(), time );
	    		intent.putExtra("time_to", fltParams.GetTimeTo());
	    		if ( time != -1 )
	    		{
	    			timeToText.setText( TimeToStr( time ) );
	    		}
	    		else
	    			timeToText.setText( "" );
	    	}
    	}
    }
    
    @Override
    public void onBackPressed()
    {
    	long sizeFrom = -1, sizeTo = -1;
		
		if ( sizeFromText.getText().length() > 0 )
		{
			long units = Long.parseLong( sizeFromText.getText().toString() );
			long measure = spinnerFrom.getSelectedItemPosition();
			sizeFrom = GetSize( units, measure );
		}
		
		if ( sizeToText.getText().length() > 0 )
		{
			long units = Long.parseLong( sizeToText.getText().toString() );
			long measure = spinnerTo.getSelectedItemPosition();
			sizeTo = GetSize( units, measure );
		}
		
		fltParams.SetSize( sizeFrom, sizeTo );
		
		String filter = editFilter.getText().toString();
		String[] names = filter.split(";");
		
		if ( names.length > 0 )
		{
			ArrayList< String > list = new ArrayList< String >();
			for( int i = 0; i < names.length; i++ )
			{
				list.add( names[i] );
			}
			fltParams.AddNames( list );
		}
		
		fltParams.SetUseFilter( useFilter.isChecked() );
		
		intent.putExtra("size_from", fltParams.GetSizeFrom());
		intent.putExtra("size_to", fltParams.GetSizeTo());
		intent.putStringArrayListExtra( "names", fltParams.GetNames() );
		intent.putExtra("use_filter", fltParams.IsUseFilter());
		
    	finish();
    }
    
	@Override
	protected void onResume()
	{
		super.onResume();
		
		intent.putExtra("size_from", fltParams.GetSizeFrom());
		intent.putExtra("size_to", fltParams.GetSizeTo());
		intent.putExtra("time_from", fltParams.GetTimeFrom());
		intent.putExtra("time_to", fltParams.GetTimeTo());
		intent.putStringArrayListExtra( "names", fltParams.GetNames() );
		intent.putExtra("use_filter", fltParams.IsUseFilter());
		
		setResult(RESULT_CANCELED, intent);
	}
}
