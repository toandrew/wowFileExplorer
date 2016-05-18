/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mars.miuifilemanager.utils;

import java.util.HashMap;
import java.util.Iterator;

/**
 * MediaScanner helper class.
 *
 * {@hide}
 */
public class MediaFile {
	// comma separated list of all file extensions supported by the media scanner
	public final static String sFileExtensions;

	// Audio file types
	public static final int FILE_TYPE_FLAC    = -1;
	public static final int FILE_TYPE_APE     = 0;
	public static final int FILE_TYPE_MP3     = 1;
	public static final int FILE_TYPE_M4A     = 2;
	public static final int FILE_TYPE_WAV     = 3;
	public static final int FILE_TYPE_AMR     = 4;
	public static final int FILE_TYPE_AWB     = 5;
	public static final int FILE_TYPE_WMA     = 6;
	public static final int FILE_TYPE_OGG     = 7;
	public static final int FILE_TYPE_AAC     = 8;
	public static final int FILE_TYPE_MKA     = 9;
	private static final int FIRST_AUDIO_FILE_TYPE = FILE_TYPE_FLAC;
	private static final int LAST_AUDIO_FILE_TYPE = FILE_TYPE_MKA;

	// MIDI file types
	public static final int FILE_TYPE_MID     = 11;
	public static final int FILE_TYPE_SMF     = 12;
	public static final int FILE_TYPE_IMY     = 13;
	private static final int FIRST_MIDI_FILE_TYPE = FILE_TYPE_MID;
	private static final int LAST_MIDI_FILE_TYPE = FILE_TYPE_IMY;

	// Video file types
	public static final int FILE_TYPE_AVI     = 17;
	public static final int FILE_TYPE_MPG     = 18;
	public static final int FILE_TYPE_MOV     = 19;
	public static final int FILE_TYPE_FLV     = 20;
	public static final int FILE_TYPE_MP4     = 21;
	public static final int FILE_TYPE_M4V     = 22;
	public static final int FILE_TYPE_3GPP    = 23;
	public static final int FILE_TYPE_3GPP2   = 24;
	public static final int FILE_TYPE_WMV     = 25;
	public static final int FILE_TYPE_ASF     = 26;
	public static final int FILE_TYPE_MKV     = 27;
	public static final int FILE_TYPE_RM      = 28;
	public static final int FILE_TYPE_MP2TS   = 29;
	private static final int FIRST_VIDEO_FILE_TYPE = FILE_TYPE_AVI;
	private static final int LAST_VIDEO_FILE_TYPE = FILE_TYPE_MP2TS;

	// Image file types
	public static final int FILE_TYPE_JPEG    = 31;
	public static final int FILE_TYPE_GIF     = 32;
	public static final int FILE_TYPE_PNG     = 33;
	public static final int FILE_TYPE_BMP     = 34;
	public static final int FILE_TYPE_WBMP    = 35;
	private static final int FIRST_IMAGE_FILE_TYPE = FILE_TYPE_JPEG;
	private static final int LAST_IMAGE_FILE_TYPE = FILE_TYPE_WBMP;

	// Playlist file types
	public static final int FILE_TYPE_M3U     = 41;
	public static final int FILE_TYPE_PLS     = 42;
	public static final int FILE_TYPE_WPL     = 43;
	private static final int FIRST_PLAYLIST_FILE_TYPE = FILE_TYPE_M3U;
	private static final int LAST_PLAYLIST_FILE_TYPE = FILE_TYPE_WPL;

	// doc files 
	public static final int FILE_TYPE_DOCX     	= 51; //application/vnd.ms-word.document.macroenabled.12
	public static final int FILE_TYPE_PDF     	= 52; //application/pdf	
	public static final int FILE_TYPE_EPUB     	= 53; //application/epub+zip
	public static final int FILE_TYPE_PPTX     	= 54; //application/vnd.openxmlformats-officedocument.presentationml.presentation
	public static final int FILE_TYPE_DOCM     	= 55; //application/vnd.ms-word.document.macroenabled.12
	public static final int FILE_TYPE_PPT     	= 56; //application/vnd.ms-powerpoint
	public static final int FILE_TYPE_XLS     	= 57; //application/vnd.ms-excel
	public static final int FILE_TYPE_XLAM     	= 58; //application/vnd.ms-excel.addin.macroenabled.12
	public static final int FILE_TYPE_TXT     	= 59; //text/plain
	public static final int FILE_TYPE_DOC     	= 60; //application/msword
	
	private static final int FIRST_DOC_FILE_TYPE = FILE_TYPE_DOCX;
	private static final int LAST_DOC_FILE_TYPE = FILE_TYPE_DOC;
	
	// zip files
	public static final int FILE_TYPE_BZ     	= 70; //application/x-bzip
	public static final int FILE_TYPE_BZ2     	= 71; //application/x-bzip2	
	public static final int FILE_TYPE_TAR     	= 72; //application/x-tar
	public static final int FILE_TYPE_GZ     	= 73; //application/x-gzip
	public static final int FILE_TYPE_ZIP     	= 74; //application/zip
	public static final int FILE_TYPE_RAR     	= 75; //application/x-rar-compressed
	public static final int FILE_TYPE_7Z     	= 76; //application/x-7z-compressed
	public static final int FILE_TYPE_JAR    	= 77; //application/x-java-archive
	
	private static final int FIRST_ZIP_FILE_TYPE = FILE_TYPE_BZ;
	private static final int LAST_ZIP_FILE_TYPE = FILE_TYPE_JAR;
	
	// application
	public static final int FILE_TYPE_APK     	= 90; //application/vnd.android.package-archive

	
	static class MediaFileType {

		int fileType;
		String mimeType;

		MediaFileType(int fileType, String mimeType) {
			this.fileType = fileType;
			this.mimeType = mimeType;
		}
	}

	private static HashMap<String, MediaFileType> sFileTypeMap 
		= new HashMap<String, MediaFileType>();
	private static HashMap<String, Integer> sMimeTypeMap 
		= new HashMap<String, Integer>();            
	static void addFileType(String extension, int fileType, String mimeType) {
		sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
		sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
	}

	private static boolean isWMAEnabled() {
		return true;
	}

	private static boolean isWMVEnabled() {
		return true;
	}

	static {
		addFileType("MP3", FILE_TYPE_MP3, "audio/mpeg");
		addFileType("M4A", FILE_TYPE_M4A, "audio/mp4");
		addFileType("3GA", FILE_TYPE_M4A, "audio/mp4");
		addFileType("WAV", FILE_TYPE_WAV, "audio/x-wav");
		addFileType("AMR", FILE_TYPE_AMR, "audio/amr");
		addFileType("AWB", FILE_TYPE_AWB, "audio/amr-wb");
		if (isWMAEnabled()) {
			addFileType("WMA", FILE_TYPE_WMA, "audio/x-ms-wma");
		}
		addFileType("OGG", FILE_TYPE_OGG, "application/ogg");
		addFileType("OGA", FILE_TYPE_OGG, "application/ogg");
		addFileType("AAC", FILE_TYPE_AAC, "audio/aac");
		addFileType("MKA", FILE_TYPE_MKA, "audio/x-matroska");
		
		addFileType("APE", FILE_TYPE_APE, "audio/x-ape");
		addFileType("FLAC", FILE_TYPE_FLAC, "audio/x-flac");
		addFileType("ADTS", FILE_TYPE_AAC, "audio/aac");

		addFileType("MID", FILE_TYPE_MID, "audio/midi");
		addFileType("MIDI", FILE_TYPE_MID, "audio/midi");
		addFileType("XMF", FILE_TYPE_MID, "audio/midi");
		addFileType("RTTTL", FILE_TYPE_MID, "audio/midi");
		addFileType("SMF", FILE_TYPE_SMF, "audio/sp-midi");
		addFileType("IMY", FILE_TYPE_IMY, "audio/imelody");
		addFileType("RTX", FILE_TYPE_MID, "audio/midi");
		addFileType("OTA", FILE_TYPE_MID, "audio/midi");

		addFileType("MPEG", FILE_TYPE_MP4, "video/mpeg");
		addFileType("MP4", FILE_TYPE_MP4, "video/mp4");
		addFileType("M4V", FILE_TYPE_M4V, "video/mp4");
		addFileType("3GP", FILE_TYPE_3GPP, "video/3gpp");
		addFileType("3GPP", FILE_TYPE_3GPP, "video/3gpp");
		addFileType("3G2", FILE_TYPE_3GPP2, "video/3gpp2");
		addFileType("3GPP2", FILE_TYPE_3GPP2, "video/3gpp2");
		addFileType("MKV", FILE_TYPE_MKV, "video/x-matroska");

		addFileType("WEBM", FILE_TYPE_MKV, "video/x-matroska");


		addFileType("AVI", FILE_TYPE_AVI, "video/vnd.avi");
		addFileType("MPG", FILE_TYPE_MPG, "video/mpeg");
		addFileType("MOV", FILE_TYPE_MOV, "video/quicktime");
		addFileType("FLV", FILE_TYPE_FLV, "video/x-flv");


		addFileType("RM", FILE_TYPE_RM, "video/x-pn-realvideo");
		addFileType("RMVB", FILE_TYPE_RM, "video/x-pn-realvideo");
		addFileType("TS", FILE_TYPE_MP2TS, "video/mp2ts");
		addFileType("APE", FILE_TYPE_APE, "audio/x-ape");
		addFileType("FLAC", FILE_TYPE_FLAC, "audio/x-flac");

		addFileType("WMV", FILE_TYPE_WMV, "video/x-ms-wmv");
		addFileType("ASF", FILE_TYPE_ASF, "video/x-ms-asf");

		addFileType("JPG", FILE_TYPE_JPEG, "image/jpeg");
		addFileType("JPEG", FILE_TYPE_JPEG, "image/jpeg");
		addFileType("GIF", FILE_TYPE_GIF, "image/gif");
		addFileType("PNG", FILE_TYPE_PNG, "image/png");
		addFileType("BMP", FILE_TYPE_BMP, "image/x-ms-bmp");
		addFileType("WBMP", FILE_TYPE_WBMP, "image/vnd.wap.wbmp");

		addFileType("M3U", FILE_TYPE_M3U, "audio/x-mpegurl");
		addFileType("PLS", FILE_TYPE_PLS, "audio/x-scpls");
		addFileType("WPL", FILE_TYPE_WPL, "application/vnd.ms-wpl");

		
		// doc file 
		addFileType("DOCX", FILE_TYPE_DOCX, "application/vnd.ms-word.document.macroenabled.12");
		addFileType("PDF", FILE_TYPE_PDF, "application/pdf");
		addFileType("EPUB", FILE_TYPE_EPUB, "application/epub+zip");
		addFileType("PPTX", FILE_TYPE_PPTX, "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		addFileType("DOCM", FILE_TYPE_DOCM, "application/vnd.ms-word.document.macroenabled.12");
		addFileType("PPT", FILE_TYPE_PPT, "application/vnd.ms-powerpoint");
		addFileType("XLS", FILE_TYPE_XLS, "application/vnd.ms-excel");
		addFileType("XLAM", FILE_TYPE_XLAM, "application/vnd.ms-excel.addin.macroenabled.12");
		addFileType("TXT", FILE_TYPE_TXT, "text/plain");
		addFileType("DOC", FILE_TYPE_DOC, "application/msword");
		
		// zip file
		addFileType("BZ", FILE_TYPE_BZ, "application/x-bzip");
		addFileType("BZ2", FILE_TYPE_BZ2, "application/x-bzip2");
		addFileType("TAR", FILE_TYPE_TAR, "application/x-tar");
		addFileType("GZ", FILE_TYPE_GZ, "application/x-gzip");
		addFileType("ZIP", FILE_TYPE_ZIP, "application/zip");
		addFileType("RAR", FILE_TYPE_RAR, "application/x-rar-compressed");
		addFileType("7Z", FILE_TYPE_7Z, "application/x-7z-compressed");
		addFileType("JAR", FILE_TYPE_JAR, "application/x-java-archive");
		
		
		addFileType("APK", FILE_TYPE_APK, "application/vnd.android.package-archive");
		
		// compute file extensions list for native Media Scanner
		StringBuilder builder = new StringBuilder();
		Iterator<String> iterator = sFileTypeMap.keySet().iterator();

		while (iterator.hasNext()) {
			if (builder.length() > 0) {
				builder.append(',');
			}
			builder.append(iterator.next());
		} 
		sFileExtensions = builder.toString();
	}

	public static boolean isAudioFileType(int fileType) {
		return ((fileType >= FIRST_AUDIO_FILE_TYPE &&
					fileType <= LAST_AUDIO_FILE_TYPE) ||
				(fileType >= FIRST_MIDI_FILE_TYPE &&
				 fileType <= LAST_MIDI_FILE_TYPE));
	}

	public static boolean isVideoFileType(int fileType) {
		return (fileType >= FIRST_VIDEO_FILE_TYPE &&
				fileType <= LAST_VIDEO_FILE_TYPE);
	}

	public static boolean isImageFileType(int fileType) {
		return (fileType >= FIRST_IMAGE_FILE_TYPE &&
				fileType <= LAST_IMAGE_FILE_TYPE);
	}

	public static boolean isPlayListFileType(int fileType) {
		return (fileType >= FIRST_PLAYLIST_FILE_TYPE &&
				fileType <= LAST_PLAYLIST_FILE_TYPE);
	}

	public static boolean isDocFileType(int fileType) {
		return (fileType >= FIRST_DOC_FILE_TYPE &&
				fileType <= LAST_DOC_FILE_TYPE);
	}
	
	public static boolean isZipFileType(int fileType) {
		return (fileType >= FIRST_ZIP_FILE_TYPE &&
				fileType <= LAST_ZIP_FILE_TYPE);
	}
	
	public static boolean isApkFileType(int fileType) {
		return (fileType == FILE_TYPE_APK);
	}
	
	
	public static MediaFileType getFileType(String path) {
		int lastDot = path.lastIndexOf(".");
		if (lastDot < 0)
			return null;
		return sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase());
	}

	public static int getFileTypeForMimeType(String mimeType) {
		Integer value = sMimeTypeMap.get(mimeType);
		return (value == null ? 0 : value.intValue());
	}

}
