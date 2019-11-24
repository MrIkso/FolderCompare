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

package com.mrikso.foldercompare.comparator;

import android.os.Handler;
import android.os.Message;

import com.mrikso.foldercompare.activity.CompareListView;
import com.mrikso.foldercompare.filecomparator.CompareInfo;
import com.mrikso.foldercompare.filecomparator.CompareStatistics;
import com.mrikso.foldercompare.filecomparator.CompareTask;
import com.mrikso.foldercompare.filecomparator.TaskCompletionHandler;

import java.util.ArrayList;
import java.util.List;

public class ComparePresentation extends TaskCompletionHandler implements Runnable {
    private ArrayList<CompareInfo> compareInfoLeft = new ArrayList<CompareInfo>();
    private ArrayList<CompareInfo> compareInfoRight = new ArrayList<CompareInfo>();
    private ArrayList<CompareItem> itemsLeft = new ArrayList<CompareItem>();
    private ArrayList<CompareItem> itemsRight = new ArrayList<CompareItem>();
    private String pathLeft, pathRight;
    private CompareStatistics statistics;

    private CompareConfig cmpConfig;
    private CompareFilter cmpFilter;
    private FilterParams fltParams;

    private CompareListView cmpList;
    private Handler mainHandler;

    private static final int pollDelay = 1000;

    private Object doneEvent = new Object();
    private boolean interrupted = false;


    public ComparePresentation(CompareListView cmpList, Handler mainHandler) {
        this.cmpList = cmpList;
        this.mainHandler = mainHandler;

        if (cmpList != null)
            SetPath(cmpList.GetPathLeft(), cmpList.GetPathRight());
    }

    private void DbgPrintResults() {
        if (itemsLeft.size() != itemsRight.size())
            System.out.println("Results are not aligned!");

        int size = Math.min(itemsLeft.size(), itemsRight.size());
        for (int i = 0; i < size; i++) {
            String nameLeft = itemsLeft.get(i).GetFileName();
            String nameRight = itemsRight.get(i).GetFileName();

            String s = "";
            s += nameLeft;
            s += itemsLeft.get(i).IsEqualToOpposite() ? " = " : " | ";
            s += nameRight;
            System.out.println(s);
        }
    }

    private void SortFiles(List<CompareItem> items) {
        ArrayList<CompareItem> sorted = new ArrayList<CompareItem>();

        for (CompareItem item : items) {
            if (item.GetFile().isDirectory()) {
                sorted.add(item);
            }
        }

        for (CompareItem item : items) {
            if (!item.GetFile().isDirectory()) {
                sorted.add(item);
            }
        }

        items.clear();
        items.addAll(sorted);
    }

    private void AlignEqualFiles() {
        int i, j;
        int pos;
        int spaceCnt;

        for (i = 0; i < itemsLeft.size(); i++) {
            if (itemsLeft.get(i).IsEmpty())
                continue;

            final String fileNameLeft = itemsLeft.get(i).GetFileName();

            pos = -1;
            for (j = 0; j < itemsRight.size(); j++) {
                final String fileNameRight = itemsRight.get(j).GetFileName();

                if (fileNameLeft.equals(fileNameRight)) {
                    pos = j;
                    break;
                }
            }

            if (pos != -1) {
                if (i < pos) {
                    spaceCnt = pos - i;
                    for (j = 0; j < spaceCnt; j++) {
                        itemsLeft.add(i, CompareItem.GetEmptyItem());
                    }
                    i += spaceCnt;
                } else {
                    spaceCnt = i - pos;
                    for (j = 0; j < spaceCnt; j++) {
                        itemsRight.add(pos, CompareItem.GetEmptyItem());
                    }
                }
            }
        }
    }

    private void AddEmptyItems() {
        int i, j;
        boolean found;

        for (i = 0; i < itemsLeft.size(); i++) {
            final String fileNameLeft = itemsLeft.get(i).GetFileName();

            found = false;
            for (j = 0; j < itemsRight.size(); j++) {
                final String fileNameRight = itemsRight.get(j).GetFileName();
                if (fileNameLeft.equals(fileNameRight)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                if ((i + 1) > itemsRight.size())
                    break;

                itemsLeft.add(i, CompareItem.GetEmptyItem());
                itemsRight.add(i + 1, CompareItem.GetEmptyItem());
                i++;
            }
        }

        int size = Math.min(itemsLeft.size(), itemsRight.size());
        for (i = 0; i < size; i++) {
            if (itemsLeft.get(i).IsEmpty() && itemsRight.get(i).IsEmpty()) {
                itemsLeft.remove(i);
                itemsRight.remove(i);
                i--;
                size--;
            }
        }
    }

    private void AlignItemsList() {
        int spaceCnt = Math.abs(itemsLeft.size() - itemsRight.size());
        if (spaceCnt == 0)
            return;

        ArrayList<CompareItem> lesserList;
        lesserList = (itemsLeft.size() < itemsRight.size()) ? itemsLeft : itemsRight;

        for (int i = 0; i < spaceCnt; i++) {
            lesserList.add(lesserList.size(), CompareItem.GetEmptyItem());
        }
    }

    private void HandleComparison(CompareTask compareTask) {
        compareTask.GetCompareInfo(compareInfoLeft, compareInfoRight);

        cmpFilter.RemoveHidenFiles();

        itemsLeft.clear();
        for (CompareInfo cmpInfo : compareInfoLeft) {
            itemsLeft.add(new CompareItem(cmpInfo));
        }

        itemsRight.clear();
        for (CompareInfo cmpInfo : compareInfoRight) {
            itemsRight.add(new CompareItem(cmpInfo));
        }

        SortFiles(itemsLeft);
        SortFiles(itemsRight);

        AlignEqualFiles();
        AddEmptyItems();
        AlignItemsList();

        if (cmpList != null)
            cmpList.SetCompareItems(itemsLeft, itemsRight);

        if (mainHandler != null) {
            Message msg = mainHandler.obtainMessage(1, "SetCompareItems");
            mainHandler.sendMessage(msg);
        }
    }

    @Override
    public void run() {
        CompareTask compareTask = new CompareTask();
        compareTask.SetPath(pathLeft, pathRight);
        compareTask.SetCompareStatistics(statistics);
        compareTask.AddDoneEvent(doneEvent);
        compareTask.AddTaskCompletionHandler(this);
        compareTask.AddProgressHandler(new CompareProgressHandler(mainHandler));

        cmpFilter = new CompareFilter(compareInfoLeft, compareInfoRight, cmpConfig, fltParams);

        Thread compareThread = new Thread(compareTask);
        compareThread.start();

        try {
            while (true) {
                if (interrupted) {
                    compareTask.Stop();
                    synchronized (doneEvent) {
                        doneEvent.wait();
                    }
                    CompletionAccepted();
                    break;
                }

                if (IsTaskCompleted()) {
                    HandleComparison(compareTask);
                    CompletionAccepted();
                    break;
                }

                synchronized (doneEvent) {
                    doneEvent.wait(pollDelay);
                }

                if (!interrupted) {
                    HandleComparison(compareTask);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        OnTaskCompleted();
    }

    private void OnTaskCompleted() {
        if (cmpList != null)
            cmpList.OnCompleteComparison();

        if (mainHandler != null) {
            Message msg = mainHandler.obtainMessage(1, "OnComparisonTaskCompleted");
            mainHandler.sendMessage(msg);
        }

        DbgPrintResults();
    }

    public void Interrupt() {
        interrupted = true;
        synchronized (doneEvent) {
            doneEvent.notify();
        }
    }

    public void SetConfig(CompareConfig config) {
        cmpConfig = config;
    }

    public void SetFilterParams(FilterParams fltParams) {
        this.fltParams = fltParams;
    }

    public void SetCompareStatistics(CompareStatistics stat) {
        statistics = stat;
    }

    private void SetPath(String pathLeft, String pathRight) {
        this.pathLeft = pathLeft;
        this.pathRight = pathRight;
    }
/*
    public static void main(String[] args) {
        ComparePresentation cmpPresentation = new ComparePresentation(null, null);
        cmpPresentation.SetPath("D:/PrjVC/DevicePro/golib/rtl", "D:/PrjVC/r/golib/rtl");
        //cmpPresentation.SetPath("C:/Windows/System32", "C:/Windows/System32");
        Thread t = new Thread(cmpPresentation);

        long s = System.currentTimeMillis();

        t.start();
		/*try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println( "Interrupt" );
		cmpPresentation.Interrupt();*/
/*
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(System.currentTimeMillis() - s);
        System.out.println("done");

    }*/

}
