import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;


public class Main
{
	// * list of SORTED not seen posts from the requested reddit url
	// * class Post sorts by score
	private ArrayList<Post> notSeenPosts;

	// the requested reddit page url
	private String pageURL;

	// time in min between searches of the requested reddit url
	private double minDelay;

	// maximum number of times the program downloads and searches the redded url
	private int maxSearchTimes;

	// number of posts to search @ each search cycle
	private int searchNum;

	// string representation of the html document of the requested reddit page
	private String htmlString;

	// * the current search position within the downloaded reddit html
	// 		(prevents double searching/adding in a single page download)
	// * it is edited by each call to find the wanted information
	private int currentSearchPos;

	// the *_Begin and *_End mark the points
	//		between which information is extract wanted information (the *)
	private String score_Begin = "\"score unvoted\">";

	private String url_Begin = "\"title \" href=\"";
	private char url_End = '\"';

	private String title_Begin = ">";
	private char title_End = '<';

	private String subreddit_Begin = "class=\"subreddit hover\">";
	private char subreddit_End = '<';

	private String comments_Begin = "class=\"comments\" href=\"";
	private char comments_End = '\"';

	private String commentNum_Begin = "target=\"_parent\">";
	private char commentNum_End = ' ';


	Main(String url, double minDelay, int maxSearchTimes, int searchNum)
	{
		notSeenPosts 			= new ArrayList<Post>();
		pageURL 				= url;
		this.minDelay 			= minDelay;
		this.maxSearchTimes 	= maxSearchTimes;
		this.searchNum 			= searchNum;

		run();
	}


	private boolean downloadPage()
	{
		boolean isNotDownloaded = true;
		while (isNotDownloaded)
		{
			try
			{
				Document d = Jsoup.connect(pageURL).get();
				htmlString = d.outerHtml();
				isNotDownloaded = false; // you just downloaded it
			}
			catch (Exception e)
			{
				System.out.println("cannot download");
				System.out.println();
			}
		}

		return !isNotDownloaded; // isNotDownloaded should be false @ success end -> NOT()
	}


	private void run()
	{

		int runTimes = 0;
		while (runTimes < maxSearchTimes)
		{
			boolean isDownloaded = downloadPage();
			assert(isDownloaded);
			if (isDownloaded)
			{
				System.out.println("downloaded (" + (runTimes + 1) + ") times");
				System.out.println();

				int added = 0;
				currentSearchPos = 0;
				while (added < searchNum)
				{
					String score = findScore(currentSearchPos);
					String url = findInformation(currentSearchPos, url_Begin,
					                             url_End);
					String title = findInformation(currentSearchPos,
					                               title_Begin, title_End);
					String subReddit = findInformation(currentSearchPos,
					                                   subreddit_Begin, subreddit_End);
					String comments = findInformation(currentSearchPos,
					                                  comments_Begin, comments_End);
					String numComments = findInformation(currentSearchPos,
					                                     commentNum_Begin, commentNum_End);

					Post p;
					try
					{
						p = new Post(Integer.parseInt(score), url, title,
						             subReddit, comments,
						             Integer.parseInt(numComments));
					}
					catch (NumberFormatException e)
					{
						System.out.println(">>> Number Error");
						System.out.println();
						continue;
					}

					boolean shouldAdd = true;
					for (int i = 0; i < notSeenPosts.size(); i++)
					{
						// if the post exists at all in notSeenPosts, don't add it
						if (notSeenPosts.get(i).equals(p))
						{
							shouldAdd = false;
							break;
						}
					}
					if (shouldAdd == true)
					{
						notSeenPosts.add(p);
						added++;
					}
				}

				Collections.sort(notSeenPosts);
			}
			runTimes++;

			if (!(runTimes < 5))
			{
				try
				{
					Thread.sleep((long)(minDelay * 60000));
				}
				catch (InterruptedException e) {}
			}

		}
//		printNotSeen();
		writeNotSeenToFile();

		new UserInteraction_linux(notSeenPosts);

	}


	public String findInformation(int startSearch, String beginning, char end)
	{
		String returnString = "";

		// pos holds the position if the first char in what you want
		int pos = htmlString.indexOf(beginning, startSearch) + beginning.length();

		char c = htmlString.charAt(pos);
		while (c != end)
		{
			returnString += c;
			pos++;
			c = htmlString.charAt(pos);
		}

		if (returnString.startsWith("/r/"))
		{
			returnString = "http://www.reddit.com" + returnString;
		}

		currentSearchPos = pos;
		return returnString;
	}


	public String findScore(int startSearch)
	{
		int scorePos = htmlString.indexOf(score_Begin, startSearch) + score_Begin.length();

		String scoreValue = "";
		for (int i = 0; i < 5; ++i)
		{
			char tempChar = htmlString.charAt(scorePos + i + 9);
			try
			{
				Integer.parseInt(Character.toString(tempChar));
			}
			catch (NumberFormatException e)
			{
				break;
			}

			scoreValue += tempChar;
		}

		currentSearchPos = scorePos;
		return scoreValue;
	}


	public void printNotSeen()
	{
		for (int i = 0; i < notSeenPosts.size(); i++)
		{
			System.out.println(notSeenPosts.get(i));
		}
	}


	public void writeNotSeenToFile()
	{
		String fileName = "notSeen.txt";
		try
		{
			PrintWriter out = new PrintWriter(fileName);
			for (int i = 0; i < notSeenPosts.size(); i++)
			{
				notSeenPosts.get(i).writeToFile(out);;
			}
			out.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File Not Found: " + fileName);
		}
	}


	public static void main(String[] args) throws IOException
	{
		if (args.length != 4)
		{
			System.out.println("usage: <url> <min search delay> <total searches> <posts to search each time>");
			System.out.println("(without the < and > of course)");
			return;
		}

		String url = args[0];

		double minDelay = 0;
		try
		{
			minDelay = Double.parseDouble(args[1]);
		}
		catch (NumberFormatException e)
		{
			System.out.println("cannot determin what number: (" + args[1] + ") is");
			System.out.println("usage: <url> <min search delay> <total searches> <posts to search each time>");
			System.out.println("(without the < and > of course)");
			return;
		}

		int maxSearchTimes = 0;
		try
		{
			maxSearchTimes = Integer.parseInt(args[2]);
		}
		catch (NumberFormatException e)
		{
			System.out.println("cannot determin what number: (" + args[2] + ") is");
			System.out.println("usage: <url> <min search delay> <total searches> <posts to search each time>");
			System.out.println("(without the < and > of course)");
			return;
		}

		int searchNum = 0;
		try
		{
			searchNum = Integer.parseInt(args[3]);
		}
		catch (NumberFormatException e)
		{
			System.out.println("cannot determin what number: (" + args[3] + ") is");
			System.out.println("usage: <url> <min search delay> <total searches> <posts to search each time>");
			System.out.println("(without the < and > of course)");
			return;
		}

		new Main(url, minDelay, maxSearchTimes, searchNum);

	}

}
