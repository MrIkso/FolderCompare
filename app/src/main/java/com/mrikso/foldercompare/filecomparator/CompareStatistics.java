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

public class CompareStatistics
{
	private int uniqueLeftCnt, uniqueRightCnt;
	private int changedFilesCnt, unchangedFilesCnt;
	private int totalCompared;
	
	public int getUniqueLeftCnt() {
		return uniqueLeftCnt;
	}
	public void setUniqueLeftCnt(int uniqueLeftCnt) {
		this.uniqueLeftCnt = uniqueLeftCnt;
	}
	public int getUniqueRightCnt() {
		return uniqueRightCnt;
	}
	public void setUniqueRightCnt(int uniqueRightCnt) {
		this.uniqueRightCnt = uniqueRightCnt;
	}
	public int getChangedFilesCnt() {
		return changedFilesCnt;
	}
	public void setChangedFilesCnt(int changedFilesCnt) {
		this.changedFilesCnt = changedFilesCnt;
	}
	public int getUnchangedFilesCnt() {
		return unchangedFilesCnt;
	}
	public void setUnchangedFilesCnt(int unchangedFilesCnt) {
		this.unchangedFilesCnt = unchangedFilesCnt;
	}
	public int getTotalCompared() {
		return totalCompared;
	}
	public void setTotalCompared(int totalCompared) {
		this.totalCompared = totalCompared;
	}
	
	public void Reset()
	{
		uniqueLeftCnt = uniqueRightCnt = 0;
		changedFilesCnt = unchangedFilesCnt = 0;
		totalCompared = 0;
	}
}
