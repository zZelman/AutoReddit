package Utils;

import java.util.Calendar;

public class Clock
{

	public static void printTime()
	{
		String hour 	= String.valueOf(Calendar.getInstance().get(Calendar.HOUR));
		String min		= String.valueOf(Calendar.getInstance().get(Calendar.MINUTE));
		String seconds	= String.valueOf(Calendar.getInstance().get(Calendar.SECOND));
		String am_pm	= null;

		int isAM = Calendar.getInstance().get(Calendar.AM);
		if (isAM == 1)
		{
			am_pm = "AM";
		}
		else
		{
			am_pm = "PM";
		}

		if (min.length() == 1)
		{
			min = "0" + min;
		}

		if (seconds.length() == 1)
		{
			seconds = "0" + seconds;
		}

		System.out.println("clock: " + hour + ":" + min + ":" + seconds + " " + am_pm);
	}

}
