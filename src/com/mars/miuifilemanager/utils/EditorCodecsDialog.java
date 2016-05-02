package com.mars.miuifilemanager.utils;

import android.content.DialogInterface;

import com.mars.miuifilemanager.ui.TextEditorActivity;

public final class EditorCodecsDialog implements
		DialogInterface.OnClickListener {

	EditorCodecsDialog(TextEditorActivity parentActivity) {
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		String codec = EditorConstants.CODEC[which].toString();
		if (TextEditorActivity.smCurrentCodec.equalsIgnoreCase(codec)) {
			return;
		}

		TextEditorActivity.smCurrentCodec = codec;
	}

}
