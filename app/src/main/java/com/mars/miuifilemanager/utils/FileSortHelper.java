package com.mars.miuifilemanager.utils;

import java.util.Comparator;
import java.util.HashMap;

import com.mars.miuifilemanager.adapter.FileInfo;

public class FileSortHelper {
	public static enum SortMethod {name, size, date, type};
	
	private SortMethod mSort;
	
	private HashMap<SortMethod, FileComparator> mComparatorList;
	
	private boolean mFileFirst;
	
	private FileComparator mCmpDate;
	
	private FileComparator mCmpName;
	
	private FileComparator mCmpSize;
	
	private FileComparator mCmpType;
	
	public FileSortHelper() {
		mComparatorList = new HashMap<SortMethod, FileComparator>();
		
		mCmpName = new FileComparator() {
			public int doCompare(FileInfo fileinfo, FileInfo fileinfo1) {
				String fileName1 = fileinfo.fileName;
				String fileName2 = fileinfo1.fileName;
				return fileName1.compareToIgnoreCase(fileName2);
			}
		};
		
		mCmpSize = new FileComparator() {
			public int doCompare(FileInfo fileinfo, FileInfo fileinfo1) {
				FileSortHelper filesorthelper = FileSortHelper.this;
				long l = fileinfo.fileSize;
				long l1 = fileinfo1.fileSize;
				long l2 = l - l1;
				return filesorthelper.longToCompareInt(l2);
			}
		};
		
		mCmpDate = new FileComparator() {
			public int doCompare(FileInfo fileinfo, FileInfo fileinfo1) {
				FileSortHelper filesorthelper = FileSortHelper.this;
				long l = fileinfo1.ModifiedDate;
				long l1 = fileinfo.ModifiedDate;
				long l2 = l - l1;
				return filesorthelper.longToCompareInt(l2);
			}
		};
		
		mCmpType = new FileComparator() {
			protected int doCompare(FileInfo fileinfo, FileInfo fileinfo1) {
				String fileExtName1 = Util.getExtFromFilename(fileinfo.fileName);
				String fileExtName2 = Util.getExtFromFilename(fileinfo1.fileName);
				int ret = fileExtName1.compareToIgnoreCase(fileExtName2);
				if (ret == 0) {
					String fileName1 = Util.getNameFromFilename(fileinfo.fileName);
					String fileName2 = Util.getNameFromFilename(fileinfo1.fileName);
					ret = fileName1.compareToIgnoreCase(fileName2);
				}
				
				return ret;
			}
		};
		
		mSort = SortMethod.name;
		
		mComparatorList.put(SortMethod.name, mCmpName);
		mComparatorList.put(SortMethod.size, mCmpSize);
		mComparatorList.put(SortMethod.date, mCmpDate);
		mComparatorList.put(SortMethod.type, mCmpType);
	}
	
	public void setSortMethog(SortMethod sortmethod) {
		mSort = sortmethod;
	}
	
	public SortMethod getSortMethod() {
		return mSort;
	}
	
	public FileComparator getComparator() {
		return (FileComparator)mComparatorList.get(mSort);
	}
	
	private abstract class FileComparator implements Comparator<FileInfo> {
		public int compare(FileInfo fileInfo1, FileInfo fileInfo2) {
			if ((!fileInfo1.IsDir && !fileInfo2.IsDir) || (fileInfo1.IsDir && fileInfo2.IsDir)) {
				return doCompare(fileInfo1, fileInfo2);
			} 

			int ret = 0;
			
			if (mFileFirst) {
				if (fileInfo1.IsDir) {
					ret = 1;
				} else {
					ret = -1;
				}
			} else {
				if (fileInfo1.IsDir) {
					ret = -1;
				} else {
					ret = 1;
				}
			}
			
			return ret;
		}
		
/*
		public int compare(Object obj, Object obj1) {
			if (obj instanceof FileInfo && obj1 instanceof FileInfo) {
				FileInfo fileinfo = (FileInfo)obj;
				FileInfo fileinfo1 = (FileInfo)obj1;
				return com(fileinfo, fileinfo1);
			} else {
				throw new UnsupportedOperationException("comparison cannot be performed");
			}

		}
*/
		protected abstract int doCompare(FileInfo fileinfo, FileInfo fileinfo1);
	}
	
	private int longToCompareInt(long l){
		int i;
		if (l > 0L){
			i = 1;
		} else if (l < 0L) {
			i = -1;
		} else {
			i = 0;
		}
		return i;
	}
}
