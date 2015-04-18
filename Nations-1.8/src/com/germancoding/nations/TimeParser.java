package com.germancoding.nations;

public class TimeParser {

	public static String secondsToDate(int n) {
		int hour = n / 60 / 60;
		int min = (n - hour * 60 * 60) / 60;
		int sec = (n - hour * 60 * 60 - min * 60);
		String hours = "" + hour;
		if (hour < 10)
			hours = "0" + hour;
		String mins = "" + min;
		if (min < 10)
			mins = "0" + min;
		String secs = "" + sec;
		if (sec < 10)
			secs = "0" + sec;
		return hours + ":" + mins + ":" + secs;
	}

	public static String hoursToDate(int n) {
		int hour = n / 60 / 60;
		int min = (n - hour * 60 * 60) / 60;
		int days = hour / 24;
		hour = hour - (days * 24);
		return days + " Tag/e und " + hour + " Stunde/n und " + min + " Minute/n";
	}

}
