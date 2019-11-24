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

import com.mrikso.foldercompare.filecomparator.ProgressHandler;

public class CompareProgressHandler extends ProgressHandler {
    private Handler mainHandler;

    public CompareProgressHandler(Handler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    protected void OnProgress() {
        if (mainHandler != null) {
            Message msg = mainHandler.obtainMessage(progress, "OnProgress");
            mainHandler.sendMessage(msg);
        }
    }
}
