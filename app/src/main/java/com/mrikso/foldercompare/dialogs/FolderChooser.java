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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.mrikso.foldercompare.R;
import com.mrikso.foldercompare.app.Utils;

public class FolderChooser extends Activity {
    private Intent intent = new Intent();

    private Context context;
    private ArrayList<String> data;

    private final static String defaultPath = "/sdcard";
    private String defaultReportName;
    private String dirPath, filePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.folder_chooser);

        context = this;

        data = new ArrayList<String>();

        Intent i = getIntent();
        defaultReportName = i.getExtras().getString("default_name");

        FileListItem fileListAdaptor = new FileListItem();
        GetListView().setAdapter(fileListAdaptor);

        ClickEventHandler clickHandler = new ClickEventHandler();
        GetListView().setOnItemClickListener(clickHandler);

        GetFileEdit().setText(defaultReportName);

        Button saveBtn = findViewById(R.id.chooser_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePath = dirPath;
                if (!filePath.equals("/"))
                    filePath += "/";
                filePath += GetFileEdit().getText();

                boolean exists = new File(filePath).exists();
                if (exists) {
                    OnFileExists();
                } else {
                    intent.putExtra("path", filePath);
                    finish();
                }
            }
        });

        OpenDir(defaultPath);
    }

    private void OnFileExists() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert;
        CharSequence[] option = {"Overwrite file", "Cancel"};
        builder.setTitle("File Already Exists");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        intent.putExtra("path", filePath);
                        finish();
                        break;

                    case 1:
                        break;
                }
            }
        });

        alert = builder.create();
        alert.show();
    }

    private TextView GetDirView() {
        Activity a = (Activity) context;
        return (TextView) a.findViewById(R.id.chooser_dir);
    }

    private ListView GetListView() {
        Activity a = (Activity) context;
        return (ListView) a.findViewById(R.id.chooser_list);
    }

    private EditText GetFileEdit() {
        Activity a = (Activity) context;
        return (EditText) a.findViewById(R.id.chooser_file_name);
    }

    private void SortFiles(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            public int compare(final File f1, final File f2) {
                boolean isDir1 = f1.isDirectory();
                boolean isDir2 = f2.isDirectory();

                if (isDir1 != isDir2)
                    return isDir1 ? -1 : 1;

                return f1.getName().compareTo(f2.getName());
            }
        });
    }

    private boolean OpenDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return false;

        if (!dir.canRead()) {
            Toast.makeText(getBaseContext(), "Can't read folder due to permissions", Toast.LENGTH_SHORT).show();
            return false;
        }

        File[] files = dir.listFiles();
        if (files == null)
            return false;

        SortFiles(files);

        data.clear();

        if (!dirPath.equals("/"))
            data.add("..");

        for (File f : files) {
            data.add(f.getName());
        }

        this.dirPath = dirPath;
        GetDirView().setText(dirPath);

        GetListView().invalidateViews();

        return true;
    }

    private boolean GoBackDir() {
        if (dirPath != "/") {
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

    @Override
    public void onBackPressed() {
        intent.putExtra("path", "");
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        intent.putExtra("path", dirPath);
        setResult(RESULT_CANCELED, intent);
    }


    private class FileListItem extends ArrayAdapter<String> {
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

            String fileName = data.get(position);

            if (fileName.equals("..")) {
                itemHolder.bottomView.setText("");
                itemHolder.topView.setText(fileName);
                itemHolder.icon.setImageResource(R.drawable.up);
                //convertView.setBackgroundColor( Color.BLACK );
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

            //convertView.setBackgroundColor( Color.BLACK );

            return convertView;
        }
    }

    private class ClickEventHandler implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            String fileName = data.get(position);
            if (fileName.equals("..")) {
                GoBackDir();
                return;
            }

            String filePath = dirPath;

            if (!filePath.equals("/"))
                filePath += "/";
            filePath += fileName;

            OpenDir(filePath);
        }
    }
}
