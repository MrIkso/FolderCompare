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

import java.util.ArrayList;

public class FilterParams {
    private long sizeFrom, sizeTo;
    private long timeFrom, timeTo;
    private ArrayList<String> filterNames;
    private boolean useFilter;

    public FilterParams() {
        sizeFrom = sizeTo = -1;
        timeFrom = timeTo = -1;
        filterNames = new ArrayList<>();
    }

    public long GetSizeFrom() {
        return sizeFrom;
    }

    public long GetSizeTo() {
        return sizeTo;
    }

    public void SetSize(long from, long to) {
        sizeFrom = from;
        sizeTo = to;
    }

    public long GetTimeFrom() {
        return timeFrom;
    }

    public long GetTimeTo() {
        return timeTo;
    }

    public void SetTime(long from, long to) {
        timeFrom = from;
        timeTo = to;
    }

    public void AddNames(ArrayList<String> names) {
        filterNames.clear();
        filterNames.addAll(names);
    }

    public ArrayList<String> GetNames() {
        return filterNames;
    }

    public void SetUseFilter(boolean use) {
        useFilter = use;
    }

    public boolean IsUseFilter() {
        return useFilter;
    }
}
