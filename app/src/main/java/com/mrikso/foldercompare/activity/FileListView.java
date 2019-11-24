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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mrikso.foldercompare.App;
import com.mrikso.foldercompare.R;
import com.mrikso.foldercompare.comparator.CompareItem;
import com.mrikso.foldercompare.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileListView {

    public final static int LIST_LEFT = 1;
    public final static int LIST_RIGHT = 2;
    private int listType;

    private Context context;
    private ArrayList<CompareItem> data;

    private final static String defaultPath = "/sdcard";
    private String dirPath;

    private ListSelector listSelector;


    public FileListView(Context context, ListSelector listSelector, int listType) {
        this.context = context;
        this.listSelector = listSelector;
        this.listType = listType;

        data = new ArrayList<>();
        FileListItem fileListAdaptor = new FileListItem();
        GetListView().setAdapter(fileListAdaptor);
        ClickEventHandler clickHandler = new ClickEventHandler();
        GetListView().setOnItemClickListener(clickHandler);
        //GetListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
       //LongClickHandler longClickHandler = new LongClickHandler();
       //GetListView().setOnItemLongClickListener(longClickHandler);
        TouchEventHandler touchHandler = new TouchEventHandler();
        GetListView().setOnTouchListener(touchHandler);
        GetDirView().setOnTouchListener(touchHandler);

        RestorePreferences();
        OpenDir(dirPath);
    }

    public ListView GetListView() {
        AppCompatActivity a = (AppCompatActivity) context;
        int resId;

        resId = (listType == LIST_LEFT) ? R.id.left_list : R.id.right_list;
        return (ListView) a.findViewById(resId);
    }

    public FileListView GetFileListView() {
        return this;
    }

    public TextView GetDirView() {
        AppCompatActivity a = (AppCompatActivity) context;
        int resId;

        resId = (listType == LIST_LEFT) ? R.id.left_dir : R.id.right_dir;
        return (TextView) a.findViewById(resId);
    }

    public void SavePreferences(SharedPreferences.Editor e) {
        if (listType == LIST_LEFT) {
            e.putString("left_path", dirPath);
        } else
            e.putString("right_path", dirPath);
    }

    private void RestorePreferences() {
        SharedPreferences p = ((AppCompatActivity) context).getPreferences(android.content.Context.MODE_PRIVATE);
        if (listType == LIST_LEFT) {
            dirPath = p.getString("left_path", null);
        } else {
            dirPath = p.getString("right_path", null);
        }

        if (dirPath == null)
            dirPath = defaultPath;
    }

    public int GetListType() {
        return listType;
    }

    public String GetDirPath() {
        return dirPath;
    }

    private void SortFiles(File[] files) {
        Arrays.sort(files, (f1, f2) -> {
            boolean isDir1 = f1.isDirectory();
            boolean isDir2 = f2.isDirectory();

            if (isDir1 != isDir2)
                return isDir1 ? -1 : 1;

            return f1.getName().compareTo(f2.getName());
        });
    }

    public boolean OpenDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return false;

        if (!dir.canRead()) {
            Toast.makeText(context, "Can't read folder due to permissions", Toast.LENGTH_SHORT).show();
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null)
            return false;

        SortFiles(files);

        data.clear();

        if (!dirPath.equals("/"))
            data.add(new CompareItem(".."));

        for (File f : files) {
            data.add(new CompareItem(f.getName()));
        }

        this.dirPath = dirPath;
        GetDirView().setText(dirPath);

        listSelector.SetAttemptExit(false);

        GetListView().invalidateViews();

        return true;
    }

    public boolean GoBackDir() {
        if (!dirPath.equals("/")) {
            int index = dirPath.lastIndexOf('/');
            if (index != -1) {
                String prevPath = dirPath.substring(0, index);
                if (index == 0)
                    prevPath = "/";
                return OpenDir(prevPath);
            }
        }

        return false;
    }

/*
    public void  toggleSelection(int position) {
        selectView(position, mSelectedItemsIds.get(position));
    }

    // Remove selection after unchecked
    public void  removeSelection() {
        mSelectedItemsIds = new  SparseBooleanArray();
        //notifyDataSetChanged();
    }

    // Item checked on selection

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position,  value);
        else

            mSelectedItemsIds.delete(position);
       // notifyDataSetChanged();
    }

    // Get number of selected item

    public int  getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public  SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

 */
    private class FileListItem extends ArrayAdapter<CompareItem> {
        private final static int KB = 1024;
        private final static int MB = KB * KB;
        private final static int GB = MB * KB;

        class ListItemHolder {
            TextView topView;
            TextView bottomView;
            ImageView icon;
        }

        FileListItem() {
            super(context, R.layout.list_item, data);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemHolder itemHolder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item, parent, false);

                itemHolder = new ListItemHolder();
                itemHolder.topView = convertView.findViewById(R.id.top_view);
                itemHolder.bottomView = convertView.findViewById(R.id.bottom_view);
                itemHolder.icon = convertView.findViewById(R.id.row_image);

                convertView.setTag(itemHolder);
            } else {
                itemHolder = (ListItemHolder) convertView.getTag();
            }

            CompareItem cmpItem = data.get(position);

            String fileName = cmpItem.GetFileName();

            if (fileName.equals("..")) {
                itemHolder.bottomView.setText("");
                itemHolder.topView.setText(fileName);
                itemHolder.icon.setImageResource(R.drawable.up);
                return convertView;
            }

            if (cmpItem.IsEmpty()) {
                itemHolder.bottomView.setText("");
                itemHolder.topView.setText("");
                itemHolder.icon.setImageResource(0);
                return convertView;
            }

            String path = dirPath;
            if (!dirPath.equals("/"))
                path += "/";
            path += fileName;
            File file = new File(path);

            String fileAttr;
            String permission = Utils.GetFilePermissions(file);

            if (file.isDirectory()) {
                int numItems = 0;
                File[] list = file.listFiles();
                if (list != null)
                    numItems = list.length;

                fileAttr = numItems + " items | " + permission;
            } else {
                String display_size;
                double size = file.length();

                if (size > GB)
                    display_size = String.format("%.2f Gb ", size / GB);
                else if (size < GB && size > MB)
                    display_size = String.format("%.2f Mb ", size / MB);
                else if (size < MB && size > KB)
                    display_size = String.format("%.2f Kb ", size / KB);
                else
                    display_size = String.format("%.2f bytes ", size);

                fileAttr = display_size + " | " + permission;
            }

            itemHolder.bottomView.setText(fileAttr);
            itemHolder.topView.setText(fileName);

            int resId;

            if (file.isDirectory()) {
                resId = R.drawable.folder;
            } else {
                resId = R.drawable.file;
            }

            itemHolder.icon.setImageResource(resId);
            return convertView;
        }
    }

    private class ClickEventHandler implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            CompareItem cmpItem = data.get(position);

            if (cmpItem.IsEmpty())
                return;

            String fileName = cmpItem.GetFileName();
            if (fileName.equals("..")) {
                GoBackDir();
                return;
            }

            String filePath = dirPath;

            if (!filePath.equals("/"))
                filePath += "/";
            filePath += fileName;


            if(new File(filePath).isDirectory()){
                OpenDir(filePath);
            }
            else{
                Toast.makeText(App.getContext(), "File Clicked", Toast.LENGTH_LONG).show();
            }
        }
    }
/*
    private class LongClickHandler implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
            CompareItem cmpItem = data.get(position);

            if (cmpItem.IsEmpty())
                return true;

            String fileName = cmpItem.GetFileName();
            if (fileName.equals("..")) {
                GoBackDir();
                return true;
            }

            String filePath = dirPath;

            if (!filePath.equals("/"))
                filePath += "/";
            filePath += fileName;

            ViewGroup vp = (ViewGroup) arg1;

            final int count = vp.getChildCount();

            for(int i = 0; i < count; i++) {
                final View child = vp.getChildAt(i);
                if(child instanceof Checkable) {
                    ((Checkable) child).setChecked(true);
                }
            }

           if(new File(filePath).isDirectory()){
               arg1.setBackgroundColor(Color.BLACK);
                //Toast.makeText(App.getContext(), "Folder long Clicked", Toast.LENGTH_LONG).show();
            }
            else{
                //Toast.makeText(App.getContext(), "File long Clicked", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    }
*/
    private class TouchEventHandler implements OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent arg1) {
            listSelector.SetView(GetFileListView());
            return false;
        }
    }
}
