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

package com.mrikso.foldercompare.report;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.mrikso.foldercompare.FileComparator.CompareStatistics;
import com.mrikso.foldercompare.R;
import com.mrikso.foldercompare.app.CompareListView;
import com.mrikso.foldercompare.comparator.CompareItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class ReportGenerator
{
	private Context context;
	private CompareListView cmpList;
	private CompareStatistics statistics;
	
	private DocumentBuilder builder;
	private Transformer htmlTransformer;
	
	
	public ReportGenerator( Context context, CompareListView cmpList, CompareStatistics statistics )
	{
		this.context = context;
		this.cmpList = cmpList;
		this.statistics = statistics;
		
		DocumentBuilderFactory docFactory;
		TransformerFactory transformerFactory;

		docFactory = DocumentBuilderFactory.newInstance();
		try
		{
			builder = docFactory.newDocumentBuilder();
		}
		catch( ParserConfigurationException ex )
		{
			ex.printStackTrace();
		}

		builder.setErrorHandler( new BuilderErrorHandler() );

		transformerFactory = TransformerFactory.newInstance();

		try
		{
			StreamSource styleSource = new StreamSource( context.getResources().openRawResource( R.raw.html ) );
			htmlTransformer = transformerFactory.newTransformer( styleSource );
		}
		catch( TransformerConfigurationException ex )
		{
			ex.printStackTrace();
		}
	}
	
	public void Generate( String filePath )
	{
		Document doc = CreateDOM();
		SaveAsHTML( doc, filePath );
	}
	
	private void CreateDocTimestamp( Document doc, Element root )
	{
		Element docItems;
		
		docItems = doc.createElement( "Timestamp" );
		docItems.setTextContent( DateFormat.getDateTimeInstance().format( new Date() ) );
		root.appendChild( docItems );
	}
	
	private void CreateDocFolders( Document doc, Element root )
	{
		Element docItems, docRow;
		String path, folder;
		
		docItems = doc.createElement( "Folders" );
		
		docRow = doc.createElement( "Folder" );
		
		path = cmpList.GetPathLeft();
		if ( !path.equals( "/" ) )
		{
			folder = path.substring( path.lastIndexOf( "/" ) + 1 );
			path = path.substring( 0, path.lastIndexOf( "/" ) + 1 );
		}
		else
		{
			folder = "";
		}
		docRow.setAttribute( "Location", path );
		docRow.setAttribute( "FolderName", folder );
		
		docItems.appendChild( docRow );
		
		docRow = doc.createElement( "Folder" );
		
		path = cmpList.GetPathRight();
		if ( !path.equals( "/" ) )
		{
			folder = path.substring( path.lastIndexOf( "/" ) + 1 );
			path = path.substring( 0, path.lastIndexOf( "/" ) + 1 );
		}
		else
		{
			folder = "";
		}
		docRow.setAttribute( "Location", path );
		docRow.setAttribute( "FolderName", folder );
		
		docItems.appendChild( docRow );
		
		root.appendChild( docItems );
	}
	
	private void CreateDocStatistics( Document doc, Element root )
	{
		Element docItems, docRow;
		
		docItems = doc.createElement( "Statistics" );
		
		docRow = doc.createElement( "Changed" );
		docRow.setTextContent( String.valueOf( statistics.getChangedFilesCnt() ) );
		docItems.appendChild( docRow );
		
		docRow = doc.createElement( "Unchanged" );
		docRow.setTextContent( String.valueOf( statistics.getUnchangedFilesCnt() ) );
		docItems.appendChild( docRow );
		
		docRow = doc.createElement( "Inserted" );
		docRow.setTextContent( String.valueOf( statistics.getUniqueLeftCnt() ) );
		docItems.appendChild( docRow );
		
		docRow = doc.createElement( "Removed" );
		docRow.setTextContent( String.valueOf( statistics.getUniqueRightCnt() ) );
		docItems.appendChild( docRow );
		
		docRow = doc.createElement( "Total" );
		docRow.setTextContent( String.valueOf( statistics.getTotalCompared() ) );
		docItems.appendChild( docRow );
		
		root.appendChild( docItems );
	}
	
	private void CreateDocCompareItem( Element docItem, CompareItem cmpItem )
	{
		if ( !cmpItem.IsEmpty() )
		{
			docItem.setTextContent( cmpItem.GetFileName() );
			docItem.setAttribute( "empty", "false" );
			docItem.setAttribute( "dir", cmpItem.GetFile().isDirectory() ? "true" : "false" );
			docItem.setAttribute( "equal", cmpItem.IsEqualToOpposite() ? "true" : "false" );
			docItem.setAttribute( "unique", cmpItem.IsUnique() ? "true" : "false" );
		}
		else
		{
			docItem.setTextContent( "" );
			docItem.setAttribute( "empty", "true" );
		}
	}
	
	private void CreateDocCompareItems( Document doc, Element root )
	{
		int i;
		int size;
		ArrayList<CompareItem> itemsLeft, itemsRight;
		Element docItems, docRow, docItem;
		
		itemsLeft = cmpList.GetItemsLeft();
		itemsRight = cmpList.GetItemsRight();
		
		docItems = doc.createElement( "CompareItems" );
		
		size = Math.min( itemsLeft.size(), itemsRight.size() );
		for( i = 0; i < size; i++ )
		{
			CompareItem left = itemsLeft.get( i );
			CompareItem right = itemsRight.get( i );
			
			if ( left.GetFileName().equals( ".." ) || right.GetFileName().equals( ".." ) )
				continue;
			
			docRow = doc.createElement( "CompareRow" );
			
			docItem = doc.createElement( "ItemLeft" );
			CreateDocCompareItem( docItem, left );
			docRow.appendChild( docItem );
			
			docItem = doc.createElement( "ItemRight" );
			CreateDocCompareItem( docItem, right );
			docRow.appendChild( docItem );
			
			docItems.appendChild( docRow );
		}
		
		root.appendChild( docItems );
	}
	
	private Document CreateDOM()
	{
		Element root;
		
		Document doc = builder.newDocument();
		doc.setXmlStandalone( true );

		root = doc.createElement( "DOCUMENT" );
		
		CreateDocTimestamp( doc, root );
		CreateDocFolders( doc, root );
		CreateDocStatistics( doc, root );
		CreateDocCompareItems( doc, root );

		doc.appendChild( root );
		
		return doc;
	}
	
	private void SaveImg( String dirName, String fileName, int resId )
	{
		Drawable image = context.getResources().getDrawable( resId );
		Bitmap bitmap = ((BitmapDrawable)image).getBitmap();
		
		FileOutputStream out;
		try
		{
			out = new FileOutputStream( dirName + "/" + fileName );
			bitmap.compress( Bitmap.CompressFormat.PNG, 100, out );
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	private void SaveHTMLResources( String filePath )
	{
		String dirName;
		dirName = filePath.substring( 0, filePath.lastIndexOf( '.' ) );
		
		File resDir = new File( dirName );
		resDir.mkdir();
		
		SaveImg( dirName, "folder.png", R.drawable.folder );
		SaveImg( dirName, "file.png", R.drawable.file );
	}
	
	private void SaveAsHTML( Document doc, String filePath )
	{
		FileOutputStream out;
		
		SaveHTMLResources( filePath );
		
		try
		{
			out = new FileOutputStream( filePath );
		}
		catch( FileNotFoundException ex )
		{
			ex.printStackTrace();
			return;
		}
		
		try
		{
			htmlTransformer.transform( new DOMSource( doc ), new StreamResult( out ) );
		}
		catch( TransformerException ex )
		{
			ex.printStackTrace();
		}

		try
		{
			out.close();
		}
		catch( IOException ex )
		{
			ex.printStackTrace();
		}
	}
	
	
	private class BuilderErrorHandler implements ErrorHandler
	{
		public void warning( SAXParseException exception ) throws SAXException
		{
			exception.printStackTrace();
			//throw new UnsupportedOperationException();
		}

		public void error( SAXParseException exception ) throws SAXException
		{
			exception.printStackTrace();
			//throw new UnsupportedOperationException();
		}

		public void fatalError( SAXParseException exception ) throws SAXException
		{
			exception.printStackTrace();
			//throw new UnsupportedOperationException();
		}
	}
}
