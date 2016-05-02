package com.mars.miuifilemanager.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class FilenameExtFilter implements FilenameFilter {

	private HashSet mExts;
	
	@Override
	public boolean accept(File arg0, String arg1) {
		// TODO Auto-generated method stub
		
		String path = arg0.getAbsolutePath();
		String destPath = path + File.separator + arg1;
		
		if ((new File(destPath).isDirectory())){
			return true;
		}

		int i = 0;
		if ((i = arg1.lastIndexOf('.')) == -1) {
			return false;
		}
		
		String sub = ((String)arg1.subSequence(i+1, arg1.length())).toLowerCase();
		
		return contains(sub);
	}

	public FilenameExtFilter(String filter[]) {
		mExts = new HashSet();
		
		if (filter == null) {
			return;
		}
		
		List list = Arrays.asList(filter);
		mExts.addAll(list);
	}
	
	public boolean contains(String s) {
		return mExts.contains(s.toLowerCase());
	}
}
