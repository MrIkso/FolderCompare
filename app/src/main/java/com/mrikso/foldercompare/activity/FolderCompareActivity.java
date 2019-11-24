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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mrikso.foldercompare.App;
import com.mrikso.foldercompare.R;
import com.mrikso.foldercompare.comparator.CompareConfig;
import com.mrikso.foldercompare.comparator.ComparePresentation;
import com.mrikso.foldercompare.comparator.FilterParams;
import com.mrikso.foldercompare.filecomparator.CompareStatistics;
import com.mrikso.foldercompare.report.ReportGenerator;
import com.mrikso.foldercompare.util.Utils;

public class FolderCompareActivity extends BaseActivity {

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
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fab;
    private FolderCompareActivity mainActivity;
    private Menu globalMenu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  Toolbar toolbar = findViewById(R.id.toolbar);

        // setSupportActionBar(toolbar);
        bottomAppBar = findViewById(R.id.bottom_App_bar);
        bottomAppBar.setFabCradleMargin(0f); //initial default value 17f
        bottomAppBar.replaceMenu(R.menu.options_menu);
        setSupportActionBar(bottomAppBar);
        fab = findViewById(R.id.fab_bottom_appbar);
        if (Utils.checkStorageAccessPermissions(this)) {
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
        RelativeLayout progress_panel = findViewById(R.id.progress_panel);
        progress_panel.setVisibility(RelativeLayout.GONE);
        fab.setOnClickListener(view -> {
            OnStartComparisonTask();
            Toast.makeText(App.getContext(), "Start Compare task", Toast.LENGTH_LONG).show();
            Log.i("FolderCompare", "Start Compare task");
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        globalMenu = menu;
        return true;
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
        globalMenu.findItem(R.id.menu_result_compare).setVisible(false);
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
            switch (requestCode) {
                case SETTINGS_COMPARE:
                    cmpConfig.SetShowOnlyEq(data.getBooleanExtra("only_eq", false));
                    cmpConfig.SetShowFilesNotExistsOnly(data.getBooleanExtra("unique", false));
                    cmpConfig.SetShowOnlyCompared(data.getBooleanExtra("compared", false));
                    cmpConfig.SetShowHidden(data.getBooleanExtra("hidden", false));
                    break;
                case SETTINGS_FILTER:
                    fltParams.SetSize(data.getLongExtra("size_from", -1), data.getLongExtra("size_to", -1));
                    fltParams.SetTime(data.getLongExtra("time_from", -1), data.getLongExtra("time_to", -1));
                    fltParams.AddNames(data.getStringArrayListExtra("names"));
                    fltParams.SetUseFilter(data.getBooleanExtra("use_filter", false));
                    break;
                case RESULT_CHOOSER:
                    String filePath = data.getStringExtra("path");
                    assert filePath != null;
                    if (filePath.length() > 0) {
                        reportGen.Generate(filePath);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void OnMenuSettings() {
        Intent settings = new Intent(FolderCompareActivity.this, SettingsActivity.class);
        startActivity(settings);
        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert;
        CharSequence[] option = {"Comparison Settings", "Filter Settings"};
        builder.setTitle("Settings");
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
                        settings_intent = new Intent(mainActivity, FilterSettingsActivity.class);
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

         */
    }

    private void OnMenuResults() {
        if (comparisonInProcess) {
            Toast.makeText(this, "Wait comparison completion", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(mainActivity, FolderChooser.class);
        intent.putExtra("default_name", "FolderComparisonReport.html");
        startActivityForResult(intent, RESULT_CHOOSER);
    }

    private void OnMenuAbout() {
        Intent intent = new Intent(mainActivity, AboutActivity.class);
        startActivity(intent);
    }

    private void OnMenuExit() {
        exiting = true;
        ClearPreferences();
        finish();
    }

    @SuppressLint("DefaultLocale")
    private void SetProgress(int percent) {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setProgress(percent);

        TextView text = findViewById(R.id.progress_percent);
        text.setText(String.format("%d%%", percent));
    }

    private int GetScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation;
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
        InitHandlers();
        TextView text = findViewById(R.id.progress_text);
        text.setText(getString(R.string.cmp_start));
        SetProgress(0);
        fab.setImageResource(R.drawable.ic_stop_black_24dp);
        fab.setOnClickListener((v) -> {
            OnStopComparisonTask();
            Toast.makeText(App.getContext(), "Stop Compare task", Toast.LENGTH_LONG).show();
            Log.i("FolderCompare", "Stop Compare task");
        });

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
            comparisonInProcess = false;
        }
    }

    public void OnComparisonTaskCompleted() {
        RelativeLayout progress_panel = findViewById(R.id.progress_panel);
        progress_panel.setVisibility(RelativeLayout.GONE);;
        comparisonInProcess = false;
        fab.setImageResource(R.drawable.ic_compare_black_24dp);
        globalMenu.findItem(R.id.menu_result_compare).setVisible(true);
        InitHandlers();
    }

    /*
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
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            /*
            case R.id.menu_compare:
                if (comparisonInProcess) {
                    OnStopComparisonTask();
                } else {
                    OnStartComparisonTask();
                }
                return true;

            case R.id.menu_stop_compare:
                OnStopComparisonTask();
                return true;

             */
            case R.id.menu_settings:
                OnMenuSettings();
                return true;

            case R.id.menu_result_compare:
                OnMenuResults();
                return true;

            case R.id.menu_about:
                OnMenuAbout();
                return true;

            case R.id.menu_exit:
                OnMenuExit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
        private class StopComparisonHandler implements OnClickListener {
            @Override
            public void onClick(View arg0) {
                OnStopComparisonTask();
            }
        }

     */
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
