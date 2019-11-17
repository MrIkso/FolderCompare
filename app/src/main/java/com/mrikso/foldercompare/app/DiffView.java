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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.LinkedList;

import diff.diff_match_patch;
import diff.diff_match_patch.Diff;

import com.mrikso.foldercompare.R;
import com.mrikso.foldercompare.dialogs.FolderChooser;

public class DiffView extends AppCompatActivity {
    private static final int MENU_SAVE_REPORT = 1;

    private static final int RESULT_CHOOSER = 12;

    private String leftPath, rightPath;
    private diff_match_patch comparator;
    private LinkedList<Diff> diffs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.diff);

            comparator = new diff_match_patch();
            Intent i = getIntent();
            leftPath = i.getExtras().getString("left_path");
            rightPath = i.getExtras().getString("right_path");

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
            //Thread thread = new Thread(CompareFiles());
    }
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;
        @Override
        protected String doInBackground(String... params) {
             String resp = CompareFiles();
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            ViewText(result);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(DiffView.this,
                    "Comparing file",
                    "Wait for compare files to complete");
        }

        @Override
        protected void onProgressUpdate(String... text) {
            //ViewText(text[0]);
        }

        private TextView GetListView() {
            return (TextView) findViewById(R.id.diff_text);
        }

        private String DiffToHTML(LinkedList<Diff> diffs) {
            String html = "";
            String s;

            for (Diff d : diffs) {
                s = d.text;
                String lines[] = s.split("\n");
                s = "";
                for (String line : lines) {
                    s += "<![CDATA[" + line + "]]><br>";
                }

                switch (d.operation) {
                    case EQUAL:
                        html += s;
                        break;

                    case INSERT:
                        html += "<br><b></b><font color=\"#0bff0b\">";
                        html += s;
                        html += "</font>";
                        break;

                    case DELETE:
                        html += "<br><b></b><font color=\"#ff0b0b\">";
                        html += s;
                        html += "</font>";
                        break;
                }
            }

            return html;
        }

        private String CompareFiles() {
            if (!new File(leftPath).canRead()) {
                Toast.makeText(DiffView.this, "Can't read file: " + leftPath, Toast.LENGTH_SHORT).show();
                return "";
            }

            if (!new File(rightPath).canRead()) {
                Toast.makeText(DiffView.this, "Can't read file: " + rightPath, Toast.LENGTH_SHORT).show();
                return "";
            }

            String text1 = Utils.ReadFileAsString(leftPath);
            String text2 = Utils.ReadFileAsString(rightPath);
            diffs = comparator.diff_main(text1, text2, true);
            comparator.diff_cleanupSemantic(diffs);
            String diffHtml = DiffToHTML(diffs);
            return diffHtml;

           // GetListView().setText(Html.fromHtml(diffHtml));
            //GetListView().setTextIsSelectable(true);
        }

        private void ViewText(String text) {
            GetListView().setText(Html.fromHtml(text));
            GetListView().setTextIsSelectable(true);
        }
    }
    private void SaveReport(String reportPath) {
        String diffHtml = comparator.diff_prettyHtml(diffs);
        String out = "";

        out += "<html><head><title>File Comparison Report</title></head>";
        out += "<body><h1><strong>File Comparison Report</strong></h1>";
        out += "<p>Produced by <strong>Folder Compare</strong>.</p>";
        out += "<p>Compared \"" + leftPath + "\" and \"" + rightPath + "\".</p>";

        out += diffHtml;

        out += "</body></html>";

        Utils.WriteStringToFile(out, reportPath);
    }

    private void OnMenuSaveReport() {
        Intent intent = new Intent(DiffView.this, FolderChooser.class);
        intent.putExtra("default_name", "FileComparisonReport.html");
        startActivityForResult(intent, RESULT_CHOOSER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            if (requestCode == RESULT_CHOOSER) {
                String filePath = data.getStringExtra("path");
                assert filePath != null;
                if (filePath.length() > 0) {
                    SaveReport(filePath);
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item;
        menu.clear();
        item = menu.add(0, MENU_SAVE_REPORT, 0, "Save Report");
        //item.setIcon(R.drawable.save);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SAVE_REPORT:
                OnMenuSaveReport();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
