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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mrikso.foldercompare.FileComparator.CompareStatistics;
import com.mrikso.foldercompare.R;
import com.mrikso.foldercompare.comparator.CompareConfig;
import com.mrikso.foldercompare.comparator.ComparePresentation;
import com.mrikso.foldercompare.comparator.FilterParams;
import com.mrikso.foldercompare.dialogs.About;
import com.mrikso.foldercompare.dialogs.CompareSettings;
import com.mrikso.foldercompare.dialogs.FilterSettings;
import com.mrikso.foldercompare.dialogs.FolderChooser;
import com.mrikso.foldercompare.report.ReportGenerator;

public class FolderCompare extends AppCompatActivity {
    private static final int MENU_COMPARISON_OP = 1;
    private static final int MENU_SETTINGS = 2;
    private static final int MENU_RESULTS = 3;
    private static final int MENU_ABOUT = 4;
    private static final int MENU_EXIT = 5;

    private static final int SETTINGS_COMPARE = 10;
    private static final int SETTINGS_FILTER = 11;
    private static final int RESULT_CHOOSER = 12;

    private FileListView leftList, rightList;
    private CompareListView cmpList;
    private ListSelector listSelector;
    private ComparisonTaskHolder cmpTaskHolder;
    private Handler mainHandler;

    private CompareConfig cmpConfig;
    private FilterParams fltParams;
    private CompareStatistics statistics;

    private ReportGenerator reportGen;

    private boolean comparisonInProcess;
    private boolean comparisonViewVisible;

    private boolean exiting;

    private FolderCompare mainActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        if(Utils.checkStorageAccessPermissions(this)) {
        } else {
            Toast.makeText(this, "Can't read folder due to permissions. Permission denied!", Toast.LENGTH_SHORT).show();
            System.exit(0);
        }
        mainActivity = this;

        cmpConfig = new CompareConfig();
        cmpConfig.SetShowHidden(true);
        fltParams = new FilterParams();
        statistics = new CompareStatistics();

        mainHandler = new Handler(new HandlerCallback());

        listSelector = new ListSelector();

        leftList = new FileListView(this, listSelector, FileListView.LIST_LEFT);
        rightList = new FileListView(this, listSelector, FileListView.LIST_RIGHT);

        listSelector.SetListView(leftList, rightList);

        cmpList = new CompareListView(this, listSelector, mainHandler);

        reportGen = new ReportGenerator(this, cmpList, statistics);

        InitHandlers();
    }

    private void InitHandlers() {
        ImageButton stopComparison = findViewById(R.id.progress_stop);
        stopComparison.setOnClickListener(new StopComparisonHandler());
    }

    private void SavePreferences() {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        leftList.SavePreferences(editor);
        rightList.SavePreferences(editor);
        editor.apply();
    }

    private void ClearPreferences() {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        OnStopComparisonTask();
        if (!exiting)
            SavePreferences();
    }

    @Override
    public void onBackPressed() {
        FileListView selected;
        boolean back = false;

        if (comparisonInProcess) {
            selected = listSelector.GetView();
            if (selected != null) {
                boolean isLeft = (selected.GetListType() == FileListView.LIST_LEFT);
                back = cmpList.GoBackDir(isLeft);
            }
        } else {
            selected = listSelector.GetView();
            if (selected != null) {
                if (comparisonViewVisible) {
                    boolean isLeft = (selected.GetListType() == FileListView.LIST_LEFT);
                    back = cmpList.GoBackDir(isLeft);
                } else {
                    back = selected.GoBackDir();
                }
            }
        }

        if (!back) {
            if (!listSelector.GetAttemptExit()) {
                Toast.makeText(this, "Press back again to quit.", Toast.LENGTH_SHORT).show();
                listSelector.SetAttemptExit(true);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            if (requestCode == SETTINGS_COMPARE) {
                cmpConfig.SetShowOnlyEq(data.getBooleanExtra("only_eq", false));
                cmpConfig.SetShowFilesNotExistsOnly(data.getBooleanExtra("unique", false));
                cmpConfig.SetShowOnlyCompared(data.getBooleanExtra("compared", false));
                cmpConfig.SetShowHidden(data.getBooleanExtra("hidden", false));
            } else if (requestCode == SETTINGS_FILTER) {
                fltParams.SetSize(data.getLongExtra("size_from", -1), data.getLongExtra("size_to", -1));
                fltParams.SetTime(data.getLongExtra("time_from", -1), data.getLongExtra("time_to", -1));
                fltParams.AddNames(data.getStringArrayListExtra("names"));
                fltParams.SetUseFilter(data.getBooleanExtra("use_filter", false));
            } else if (requestCode == RESULT_CHOOSER) {
                String filePath = data.getStringExtra("path");
                if (filePath.length() > 0) {
                    reportGen.Generate(filePath);
                }
            }
        }
    }

    private void OnMenuSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert;
        CharSequence[] option = {"Comparison Settings", "Filter Settings"};
        builder.setTitle("Settings");
        //builder.setIcon(R.drawable.settings);
        builder.setItems(option, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent settings_intent;
                switch (which) {
                    case 0:
                        settings_intent = new Intent(mainActivity, CompareSettings.class);
                        settings_intent.putExtra("only_eq", cmpConfig.GetShowOnlyEq());
                        settings_intent.putExtra("unique", cmpConfig.GetShowFilesNotExistsOnly());
                        settings_intent.putExtra("compared", cmpConfig.GetShowOnlyCompared());
                        settings_intent.putExtra("hidden", cmpConfig.GetShowHidden());
                        startActivityForResult(settings_intent, SETTINGS_COMPARE);
                        break;

                    case 1:
                        settings_intent = new Intent(mainActivity, FilterSettings.class);
                        settings_intent.putExtra("size_from", fltParams.GetSizeFrom());
                        settings_intent.putExtra("size_to", fltParams.GetSizeTo());
                        settings_intent.putExtra("time_from", fltParams.GetTimeFrom());
                        settings_intent.putExtra("time_to", fltParams.GetTimeTo());
                        settings_intent.putStringArrayListExtra("names", fltParams.GetNames());
                        settings_intent.putExtra("use_filter", fltParams.IsUseFilter());
                        startActivityForResult(settings_intent, SETTINGS_FILTER);
                        break;
                }
            }
        });

        alert = builder.create();
        alert.show();
    }

    private void OnMenuResults() {
        if (comparisonInProcess) {
            Toast.makeText(this, "Wait comparison completion", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert;
        CharSequence[] option = {"Generate Report"};
        builder.setTitle("Comparison Results");
        //builder.setIcon(R.drawable.save);
        builder.setItems(option, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(mainActivity, FolderChooser.class);
                        intent.putExtra("default_name", "FolderComparisonReport.html");
                        startActivityForResult(intent, RESULT_CHOOSER);
                        break;
                }
            }
        });

        alert = builder.create();
        alert.show();
    }

    private void OnMenuAbout() {
        Intent intent = new Intent(mainActivity, About.class);
        startActivity(intent);
    }

    private void OnMenuExit() {
        exiting = true;
        ClearPreferences();
        finish();
    }

    private void SetProgress(int percent) {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setProgress(percent);

        TextView text = findViewById(R.id.progress_percent);
        text.setText(percent + "%");
    }

    private int GetScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    public void OnStartComparisonTask() {
        comparisonInProcess = true;
        cmpList.SetPath(leftList.GetDirPath(), rightList.GetDirPath());
        ComparePresentation cmpPresentation = new ComparePresentation(cmpList, mainHandler);
        cmpPresentation.SetConfig(cmpConfig);
        cmpPresentation.SetFilterParams(fltParams);
        statistics.Reset();
        cmpPresentation.SetCompareStatistics(statistics);
        cmpTaskHolder = new ComparisonTaskHolder(cmpPresentation);
        cmpList.OnStartComparison(cmpTaskHolder);

        TextView text = findViewById(R.id.progress_text);
        text.setText(getString(R.string.cmp_start));
        SetProgress(0);

        RelativeLayout progress_panel = findViewById(R.id.progress_panel);
        progress_panel.setVisibility(RelativeLayout.VISIBLE);

        int orient = GetScreenOrientation();
        setRequestedOrientation(orient);

        new Thread(cmpPresentation).start();
    }

    public void OnStopComparisonTask() {
        if (cmpTaskHolder != null) {
            TextView text = findViewById(R.id.progress_text);
            text.setText(getString(R.string.cmp_stop));
            cmpTaskHolder.Interrupt();
            cmpTaskHolder = null;
        }
    }

    public void OnComparisonTaskCompleted() {
        RelativeLayout progress_panel = findViewById(R.id.progress_panel);
        progress_panel.setVisibility(RelativeLayout.GONE);
        comparisonInProcess = false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item;
        menu.clear();
        if (comparisonInProcess) {
            item = menu.add(0, MENU_COMPARISON_OP, 0, "Stop Comparison");
           // item.setIcon(R.drawable.block);
        } else {
            item = menu.add(0, MENU_COMPARISON_OP, 0, "Start Comparison");
           // item.setIcon(R.drawable.start);
        }

        item = menu.add(0, MENU_SETTINGS, 0, "Settings");
       // item.setIcon(R.drawable.settings);

        if (comparisonViewVisible) {
            item = menu.add(0, MENU_RESULTS, 0, "Results");
           // item.setIcon(R.drawable.save);
        }

        item = menu.add(0, MENU_ABOUT, 0, "About");
       // item.setIcon(R.drawable.info);
        item = menu.add(0, MENU_EXIT, 0, "Exit");
       // item.setIcon(R.drawable.exit);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_COMPARISON_OP:
                if (comparisonInProcess) {
                    OnStopComparisonTask();
                } else {
                    OnStartComparisonTask();
                }
                return true;

            case MENU_SETTINGS:
                OnMenuSettings();
                return true;

            case MENU_RESULTS:
                OnMenuResults();
                return true;

            case MENU_ABOUT:
                OnMenuAbout();
                return true;

            case MENU_EXIT:
                OnMenuExit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class StopComparisonHandler implements OnClickListener {
        @Override
        public void onClick(View arg0) {
            OnStopComparisonTask();
        }
    }

    private class HandlerCallback implements Callback {
        public boolean handleMessage(Message msg) {
            String s = (String) msg.obj;
            if (s.equals("SetCompareItems")) {
                LinearLayout panels = findViewById(R.id.panels);
                panels.setVisibility(LinearLayout.GONE);
                LinearLayout cmp_panels = findViewById(R.id.cmp_panels);
                cmp_panels.setVisibility(LinearLayout.VISIBLE);
                comparisonViewVisible = true;

                cmpList.GetListView().invalidateViews();
            } else if (s.startsWith("OpenDir")) {
                LinearLayout panels = findViewById(R.id.panels);
                panels.setVisibility(LinearLayout.VISIBLE);
                LinearLayout cmp_panels = findViewById(R.id.cmp_panels);
                cmp_panels.setVisibility(LinearLayout.GONE);
                comparisonViewVisible = false;

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

                String dirPath = s.substring("OpenDir".length());

                if (msg.what == FileListView.LIST_LEFT) {
                    leftList.OpenDir(dirPath);
                } else {
                    rightList.OpenDir(dirPath);
                }
            } else if (s.equals("OnComparisonTaskCompleted")) {
                OnComparisonTaskCompleted();
            } else if (s.equals("OnProgress")) {
                SetProgress(msg.what);
            }
            return false;
        }
    }
}
