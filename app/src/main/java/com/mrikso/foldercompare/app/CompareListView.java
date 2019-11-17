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

package com.mrikso.foldercompare.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mrikso.foldercompare.R;
import com.mrikso.foldercompare.comparator.CompareItem;

import java.io.File;
import java.util.ArrayList;

public class CompareListView {
    private Context context;
    private CompareListItem cmpListAdaptor;
    private ArrayList<CompareItem> dataLeft, dataRight;
    private Object dataMutex;

    private String pathLeft, pathRight;

    private ListSelector listSelector;

    private boolean confirmOpen;
    private String confirmOpenPath;

    private Handler mainHandler;
    private ComparisonTaskHolder cmpTaskHolder;
    private boolean comparisonInProcess;

    private int touchX;
    private int displayWidth;


    public CompareListView(Context context, ListSelector listSelector, Handler mainHandler) {
        this.context = context;
        this.listSelector = listSelector;
        this.mainHandler = mainHandler;

        dataMutex = new Object();

        dataLeft = new ArrayList<>();
        dataRight = new ArrayList<>();
        cmpListAdaptor = new CompareListItem();
        GetListView().setAdapter(cmpListAdaptor);

        ClickEventHandler clickHandler = new ClickEventHandler();
        GetListView().setOnItemClickListener(clickHandler);

        LongClickHandler longClickHandler = new LongClickHandler();
        GetListView().setOnItemLongClickListener(longClickHandler);

        TouchEventHandler touchHandler = new TouchEventHandler();
        GetListView().setOnTouchListener(touchHandler);

        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);
        displayWidth = displaymetrics.widthPixels;
    }

    public ListView GetListView() {
        Activity a = (Activity) context;
        return (ListView) a.findViewById(R.id.cmp_list);
    }

    public void SetPath(String pathLeft, String pathRight) {
        this.pathLeft = pathLeft;
        this.pathRight = pathRight;
    }

    public String GetPathLeft() {
        return pathLeft;
    }

    public String GetPathRight() {
        return pathRight;
    }

    public void OnStartComparison(ComparisonTaskHolder cmpTaskHolder) {
        this.cmpTaskHolder = cmpTaskHolder;
        comparisonInProcess = true;
    }

    public void OnCompleteComparison() {
        comparisonInProcess = false;
        cmpTaskHolder = null;
    }

    public void SetCompareItems(ArrayList<CompareItem> itemsLeft, ArrayList<CompareItem> itemsRight) {
        synchronized (dataMutex) {
            dataLeft.clear();
            dataLeft.add(new CompareItem(".."));
            dataLeft.addAll(itemsLeft);

            dataRight.clear();
            dataRight.add(new CompareItem(".."));
            dataRight.addAll(itemsRight);
        }
    }

    public ArrayList<CompareItem> GetItemsLeft() {
        return dataLeft;
    }

    public ArrayList<CompareItem> GetItemsRight() {
        return dataRight;
    }

    private void ShowConfirmDlg(String dirPath) {
        confirmOpenPath = dirPath;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog alert;
        CharSequence[] option = {"Wait comparison completion", "Open"};
        builder.setTitle("Open folder");
        builder.setCancelable(false);
        builder.setItems(option, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        confirmOpen = false;
                        break;

                    case 1:
                        if (cmpTaskHolder != null)
                            cmpTaskHolder.Interrupt();
                        confirmOpen = true;
                        OpenDir(confirmOpenPath);
                        confirmOpen = false;
                        break;
                }
            }
        });

        alert = builder.create();
        alert.show();
    }

    public boolean OpenDir(String dirPath) {
        if (comparisonInProcess && !confirmOpen) {
            ShowConfirmDlg(dirPath);
            return false;
        }

        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return false;

        if (!dir.canRead()) {
            Toast.makeText(context, "Can't read folder due to permissions", Toast.LENGTH_SHORT).show();
            return false;
        }

        Message msg = mainHandler.obtainMessage(listSelector.GetListType(), "OpenDir" + dirPath);
        mainHandler.sendMessage(msg);
        return true;
    }

    public boolean GoBackDir(boolean isLeft) {
        String dirPath = isLeft ? pathLeft : pathRight;

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


    private class CompareListItem extends ArrayAdapter<CompareItem> {
        private final static int KB = 1024;
        private final static int MB = KB * KB;
        private final static int GB = MB * KB;

        class CompareItemHolder {
            TextView topViewLeft, topViewRight;
            TextView bottomViewLeft, bottomViewRight;
            ImageView iconLeft, iconRight;
        }

        CompareListItem() {
            super(context, R.layout.cmp_item, dataLeft);
        }

        @SuppressLint("DefaultLocale")
        private void getItemView(View convertView, CompareItemHolder itemHolder, CompareItem cmpItem, boolean isLeft) {
            String fileName = cmpItem.GetFileName();

            TextView bottomView = isLeft ? itemHolder.bottomViewLeft : itemHolder.bottomViewRight;
            TextView topView = isLeft ? itemHolder.topViewLeft : itemHolder.topViewRight;
            ImageView icon = isLeft ? itemHolder.iconLeft : itemHolder.iconRight;

            if (fileName.equals("..")) {
                bottomView.setText("");
                topView.setText(fileName);
                icon.setImageResource(R.drawable.up);
                //convertView.setBackgroundColor( Color.BLACK );
                return;
            }

            if (cmpItem.IsEmpty()) {
                bottomView.setText("");
                topView.setText("");
                icon.setImageResource(0);
                //convertView.setBackgroundColor( Color.BLACK );
                return;
            }

            String path = isLeft ? pathLeft : pathRight;
            if (!path.equals("/"))
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

            bottomView.setText(fileAttr);
            topView.setText(fileName);

            int resId;

            if (file.isDirectory()) {
                resId = R.drawable.folder;
            } else {
                resId = R.drawable.file;
            }

            icon.setImageResource(resId);

            if (cmpItem.IsCompared() && !cmpItem.IsUnique()) {
                if (cmpItem.IsEqualToOpposite())
                    convertView.setBackgroundColor(context.getResources().getColor(R.color.equalFiles));//цвет содержащихся файлов
                else
                    convertView.setBackgroundColor(context.getResources().getColor(R.color.diffFiles));//цвет различных файлов
            } else {
                convertView.setBackgroundColor(context.getResources().getColor(R.color.newFiles));//цвет новых елементов
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CompareItemHolder itemHolder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.cmp_item, parent, false);

                itemHolder = new CompareItemHolder();
                itemHolder.topViewLeft = convertView.findViewById(R.id.top_view_left);
                itemHolder.bottomViewLeft = convertView.findViewById(R.id.bottom_view_left);
                itemHolder.iconLeft = convertView.findViewById(R.id.row_image_left);
                itemHolder.topViewRight = convertView.findViewById(R.id.top_view_right);
                itemHolder.bottomViewRight = convertView.findViewById(R.id.bottom_view_right);
                itemHolder.iconRight = convertView.findViewById(R.id.row_image_right);

                convertView.setTag(itemHolder);
            } else {
                itemHolder = (CompareItemHolder) convertView.getTag();
            }

            CompareItem cmpItemLeft, cmpItemRight;
            synchronized (dataMutex) {
                if (position >= dataLeft.size() || position >= dataRight.size())
                    return convertView;

                cmpItemLeft = dataLeft.get(position);
                cmpItemRight = dataRight.get(position);
            }

            getItemView(convertView, itemHolder, cmpItemLeft, true);
            getItemView(convertView, itemHolder, cmpItemRight, false);

            return convertView;
        }
    }

    private class ClickEventHandler implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
            boolean isLeft;
            CompareItem cmpItem;
            synchronized (dataMutex) {
                if (touchX < displayWidth / 2) {
                    cmpItem = dataLeft.get(position);
                    isLeft = true;
                } else {
                    cmpItem = dataRight.get(position);
                    isLeft = false;
                }
            }

            if (cmpItem.IsEmpty())
                return;

            String fileName = cmpItem.GetFileName();
            if (fileName.equals("..")) {
                GoBackDir(isLeft);
                return;
            }

            String filePath = isLeft ? pathLeft : pathRight;

            if (!filePath.equals("/"))
                filePath += "/";
            filePath += fileName;

            OpenDir(filePath);
        }
    }

    private class TouchEventHandler implements OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent arg1) {
            touchX = (int) arg1.getRawX();

            if (touchX < displayWidth / 2) {
                listSelector.SetView(FileListView.LIST_LEFT);
            } else {
                listSelector.SetView(FileListView.LIST_RIGHT);
            }
            return false;
        }
    }

    private class LongClickHandler implements OnItemLongClickListener {
        private String leftPath, rightPath;

        /*
        //Compare files
        private void ShowDlg() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            AlertDialog alert;
            CharSequence[] option = {"Compare Files"};
            builder.setTitle("File Difference");
          //  builder.setIcon(R.drawable.cmp_files);
            builder.setItems(option, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            Intent intent = new Intent(context, DiffView.class);
                            intent.putExtra("left_path", leftPath);
                            intent.putExtra("right_path", rightPath);
                            context.startActivity(intent);
                            break;
                    }
                }
            });
            alert = builder.create();
            alert.show();

        } */

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
            CompareItem cmpItemLeft, cmpItemRight;
            synchronized (dataMutex) {
                cmpItemLeft = dataLeft.get(position);
                cmpItemRight = dataRight.get(position);
            }

            if (!cmpItemLeft.IsEmpty() && cmpItemLeft.IsCompared() &&
                    !cmpItemLeft.IsUnique() && !cmpItemLeft.IsEqualToOpposite()) {
                leftPath = cmpItemLeft.GetFilePath();
                rightPath = cmpItemRight.GetFilePath();
                if(new File(leftPath).isDirectory() && new File(rightPath).isDirectory()){
                   // Intent intent = new Intent(context, FolderCompare.class);
                   // context.startActivity(intent);
                    //FolderCompare folderCompare = new FolderCompare();
                    //folderCompare.OnStartComparisonTask(leftPath, rightPath);
                    Toast.makeText(context, "Not compared!", Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(context, DiffView.class);
                    intent.putExtra("left_path", leftPath);
                    intent.putExtra("right_path", rightPath);
                    context.startActivity(intent);
                }

                //ShowDlg();
            }
            return false;
        }
    }
}
