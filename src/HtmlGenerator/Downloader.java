package HtmlGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;


public class Downloader
{
	public static final String fileOutputName = "downloaded posts.txt";

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


	Downloader(String url, double minDelay, int maxSearchTimes, int searchNum)
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

				// debug save the file
				PrintWriter out = new PrintWriter("downloaded reddit.html");
				out.print(htmlString);
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

					// * prossessedScore and prossessedNumComments are nessisary just in case
					//		the post is new and Reddit has not posted the values in the html doc
					// * this is taken into account in the .equals(other) method below
					//		jist: if it is the same, but the other is higher, update the data

					int prossessedScore = 0;
					try
					{
						prossessedScore = Integer.parseInt(score);
					}
					catch (NumberFormatException e)
					{
						prossessedScore = 0;
					}

					int prossessedNumComments = 0;
					try
					{
						prossessedNumComments = Integer.parseInt(numComments);
					}
					catch (NumberFormatException e)
					{
						prossessedNumComments = 0;
					}

					Post p = new Post(prossessedScore, url, title,
					                  subReddit, comments,
					                  prossessedNumComments);

					boolean shouldAdd = true;
					for (int i = 0; i < notSeenPosts.size(); i++)
					{
						Post tempPost = notSeenPosts.get(i);

						// if the post exists at all in notSeenPosts, don't add it
						if (tempPost.equals(p))
						{
							// these two if statements exist because this is the minimum second time
							//		that this bot has seen the post. Therefore, you check to see if the
							// 		score or numComments are greater in the one just seen than the one
							//		that you have, if yes, update them.

							if (p.score > tempPost.score)
							{
								tempPost.score = p.score;
							}

							if (p.numComments > tempPost.numComments)
							{
								tempPost.numComments = p.numComments;
							}

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
		writeToFile();

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


	public void printArrayList()
	{
		for (int i = 0; i < notSeenPosts.size(); i++)
		{
			System.out.println(notSeenPosts.get(i));
		}
	}


	public void writeToFile()
	{
		File f = new File(fileOutputName);
		if (!f.exists())
		{
			try
			{
				f.createNewFile();
			}
			catch (IOException e)
			{
				System.out.println("Could not create: " + fileOutputName);
				return;
			}
		}

		try
		{
			PrintWriter out = new PrintWriter(fileOutputName);
			for (int i = 0; i < notSeenPosts.size(); i++)
			{
				notSeenPosts.get(i).writeToFile(out);;
			}
			out.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File Not Found: " + fileOutputName);
		}
	}


	public static void main(String[] args)
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

		new Downloader(url, minDelay, maxSearchTimes, searchNum);

	}

}
