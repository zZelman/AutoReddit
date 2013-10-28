package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class ConfigParser
{
	// config file name
	public static final String configFileName = "bot.config";

	// flags from the config file
	public static final String redditUrl 		= "reddit url=";
	public static final String totalTime 		= "total time available=";
	public static final String searchInterval 	= "search interval=";
	public static final String numPostAdd 		= "uniqe posts to add at each search=";


	public static String findURL(String fileName) throws FileNotFoundException
	{
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext())
		{
			String line = scan.nextLine();

			if (line.startsWith("//"))
			{
				continue;
			}
			if (line.length() == 0)
			{
				continue;
			}

			if (line.contains(redditUrl))
			{
				// return the rest of the line
				String possibleURL = line.substring(redditUrl.length(), line.length());
				if (possibleURL.contains("reddit.com"))
				{
					return possibleURL;
				}
				else
				{
					System.out.println("Invalid url request:");
					System.out.println("reddit url=" + possibleURL);
					System.exit(-1);
				}
			}
		}

		throw new FileNotFoundException();
	}


	public static double findTotalTime(String fileName) throws FileNotFoundException
	{
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext())
		{
			String line = scan.nextLine();

			if (line.startsWith("//"))
			{
				continue;
			}
			if (line.length() == 0)
			{
				continue;
			}

			if (line.contains(totalTime))
			{
				// return the rest of the line
				try
				{
					return Double.parseDouble(line.substring(totalTime.length(), line.length()));
				}
				catch (NumberFormatException e)
				{
					System.out.println("Invalid total time request:");
					System.out.println("total time avaialbe=" +
					                   line.substring(totalTime.length(), line.length()));
					System.exit(-1);
				}
			}
		}

		throw new FileNotFoundException();
	}


	public static double findSearchInterval(String fileName) throws FileNotFoundException
	{
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext())
		{
			String line = scan.nextLine();

			if (line.startsWith("//"))
			{
				continue;
			}
			if (line.length() == 0)
			{
				continue;
			}

			if (line.contains(searchInterval))
			{
				// return the rest of the line
				try
				{
					return Double.parseDouble(line.substring(searchInterval.length(), line.length()));
				}
				catch (NumberFormatException e)
				{
					System.out.println("Invalid search interval request:");
					System.out.println("search interval=" +
					                   line.substring(totalTime.length(), line.length()));
					System.exit(-1);
				}
			}
		}

		throw new FileNotFoundException();
	}


	public static int findNumPostAdd(String fileName) throws FileNotFoundException
	{
		Scanner scan = new Scanner(new File(fileName));
		while (scan.hasNext())
		{
			String line = scan.nextLine();

			if (line.startsWith("//"))
			{
				continue;
			}
			if (line.length() == 0)
			{
				continue;
			}

			if (line.contains(numPostAdd))
			{
				// return the rest of the line
				try
				{
					return (int) Double.parseDouble(line.substring(numPostAdd.length(), line.length()));
				}
				catch (NumberFormatException e)
				{
					System.out.println("Invalid unique post add request:");
					System.out.println("uniqe posts to add at each search=" +
					                   line.substring(totalTime.length(), line.length()));
					System.exit(-1);
				}
			}
		}

		throw new FileNotFoundException();
	}
}
