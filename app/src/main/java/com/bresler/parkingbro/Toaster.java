package com.bresler.parkingbro;

import android.content.Context;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class Toaster {

	public static void info(Context context, String text) {
		Toasty.info(context, text, Toast.LENGTH_SHORT, true).show();
	}

	public static void error(Context context, String text) {
		Toasty.error(context, text, Toast.LENGTH_SHORT, true).show();
	}

	public static void success(Context context, String text) {
		Toasty.success(context, text, Toast.LENGTH_SHORT, true).show();
	}
}
