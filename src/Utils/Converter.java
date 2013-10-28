package Utils;

public class Converter
{
	public static long minToMS(double mins)
	{
		return (long)(mins * 60000);
	}

	public static double msToMin(long ms)
	{
		return (double)(ms / 60000);
	}

	public static long nsToMS(long ns)
	{
		return ns / 1000000;
	}
}
