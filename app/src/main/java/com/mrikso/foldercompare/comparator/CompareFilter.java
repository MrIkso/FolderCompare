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

import com.mrikso.foldercompare.filecomparator.CompareInfo;

import java.util.ArrayList;
import java.util.Date;

class CompareFilter {
    private ArrayList<CompareInfo> compareInfoLeft, compareInfoRight;
    private CompareConfig cmpConfig;
    private FilterParams fltParams;


    CompareFilter(ArrayList<CompareInfo> compareInfoLeft, ArrayList<CompareInfo> compareInfoRight,
                  CompareConfig cmpConfig, FilterParams fltParams) {
        this.compareInfoLeft = compareInfoLeft;
        this.compareInfoRight = compareInfoRight;
        this.cmpConfig = cmpConfig;
        this.fltParams = fltParams;
    }

    private void RemoveNotUniqueFiles(ArrayList<CompareInfo> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            CompareInfo cmpInfo = list.get(i);
            if (!cmpInfo.IsUnique()) {
                list.remove(i);
                i--;
                size--;
            }
        }
    }

    private void RemoveNotEqual(ArrayList<CompareInfo> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            CompareInfo cmpInfo = list.get(i);
            if (!cmpInfo.IsEqualToOpposite()) {
                list.remove(i);
                i--;
                size--;
            }
        }
    }

    private void RemoveNotCompared(ArrayList<CompareInfo> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            CompareInfo cmpInfo = list.get(i);
            if (!cmpInfo.IsCompared()) {
                list.remove(i);
                i--;
                size--;
            }
        }
    }

    private void RemoveHidden(ArrayList<CompareInfo> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            CompareInfo cmpInfo = list.get(i);
            if (cmpInfo.GetFile().isHidden()) {
                list.remove(i);
                i--;
                size--;
            }
        }
    }

    private boolean FilterTest(CompareInfo cmpInfo) {
        if (!cmpInfo.GetFile().isDirectory()) {
            long size = cmpInfo.GetFile().length();

            long sizeFrom = fltParams.GetSizeFrom();
            if (sizeFrom != -1) {
                if (size < sizeFrom)
                    return true;
            }

            long sizeTo = fltParams.GetSizeTo();
            if (sizeTo != -1) {
                if (size > sizeTo)
                    return true;
            }
        }

        long time = cmpInfo.GetFile().lastModified();
        Date modified = new Date(time);

        if (fltParams.GetTimeFrom() != -1) {
            if (modified.before(new Date(fltParams.GetTimeFrom())))
                return true;
        }

        if (fltParams.GetTimeTo() != -1) {
            if (modified.after(new Date(fltParams.GetTimeTo())))
                return true;
        }

        for (String filter : fltParams.GetNames()) {
            String regex = filter.replace("?", ".?").replace("*", ".*?");
            if (cmpInfo.GetFile().getName().matches(regex))
                return true;
        }

        return false;
    }

    private void Filter(ArrayList<CompareInfo> list) {
        int size = list.size();
        for (int i = 0; i < size; i++) {
            CompareInfo cmpInfo = list.get(i);
            if (FilterTest(cmpInfo)) {
                list.remove(i);
                i--;
                size--;
            }
        }
    }

    public void RemoveHidenFiles() {
        if (cmpConfig.GetShowFilesNotExistsOnly()) {
            RemoveNotUniqueFiles(compareInfoLeft);
            RemoveNotUniqueFiles(compareInfoRight);
        }

        if (cmpConfig.GetShowOnlyEq()) {
            RemoveNotEqual(compareInfoLeft);
            RemoveNotEqual(compareInfoRight);
        }

        if (cmpConfig.GetShowOnlyCompared()) {
            RemoveNotCompared(compareInfoLeft);
            RemoveNotCompared(compareInfoRight);
        }

        if (!cmpConfig.GetShowHidden()) {
            RemoveHidden(compareInfoLeft);
            RemoveHidden(compareInfoRight);
        }

        if (fltParams.IsUseFilter()) {
            Filter(compareInfoLeft);
            Filter(compareInfoRight);
        }
    }
}
