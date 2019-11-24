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

package com.mrikso.foldercompare.filecomparator;

import java.io.File;

public class CompareInfo implements Cloneable {
    public boolean IsCompared() {
        return compared;
    }

    public void SetCompared(boolean compared) {
        this.compared = compared;
    }

    public boolean IsUnique() {
        return unique;
    }

    public void SetUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean IsEqualToOpposite() {
        return equalToOpposite;
    }

    public void SetEqualToOpposite(boolean equalToOpposite) {
        this.equalToOpposite = equalToOpposite;
    }

    public File GetFile() {
        return file;
    }

    public void SetFile(File f) {
        file = f;
    }

    public CompareInfo clone() throws CloneNotSupportedException {
        CompareInfo newC = (CompareInfo) super.clone();
        newC.unique = unique;
        newC.equalToOpposite = equalToOpposite;
        newC.compared = compared;
        if (file != null)
            newC.file = new File(file.getAbsolutePath());
        return newC;
    }

    private boolean unique = true;
    private boolean equalToOpposite = false;
    private boolean compared = false;
    private File file;
}
