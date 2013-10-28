package HtmlGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import Utils.Clock;
import Utils.Converter;


public class Downloader
{
	public static final String fileOutputName = "downloaded posts.txt";


	// * this variable caps the maximum number of network disconnects
	// * this variable is useful because what if network shuts off during run time?
	//		you still want to salvage the previously downloaded links
	private static final int maximumConsecutiveNoDownloads = 50;



	// * list of SORTED not seen posts from the requested reddit url
	// * class Post sorts by score
	private ArrayList<Post> notSeenPosts;

	// the requested reddit page url
	private final String pageURL;

	// * boolean to keep track of if this is a reddit page with more than one subreddit
	// * see downloadPage() for how it is used
	private boolean isMultiReddit;

	// * total time available to do work
	// * represented in MS
	private final long totalTime;

	// * time in min between searches of the requested reddit url
	// * represented in MS
	private final long searchInterval;

	// number of posts to search @ each search cycle
	private final int searchNum;



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

	private String emptyComments_Begin = "<a class=\"comments empty\" href=\"";
	private char emptyComments_End = '\"';

	private String commentNum_Begin = "target=\"_parent\">";
	private char commentNum_End = ' ';

	// * These are the flags that allow the bot to jump between pages when the user requests > 25
	//		unique links to be added each search iteration
	// * The formating of Reddit changes between the first page and all others (jumping between pages)
	//		therefore, there needs to be two flags, nextPageFirstTime_* and nextPageAllOther_* and a
	//		boolean to switch between the two isFirstPage
	private boolean isFirstPage;
	private String nextPageFirstTime_Begin = "view more: <a href=\"";
	private char nextPageFirstTime_End = '\"';
	private String nextPageAllOther_Begin = "class=\"separator\"></span><a href=\"";
	private char nextPageAllOther_End = '\"';


	Downloader(String url, double totalTime, double searchInterval, int searchNum)
	{
		notSeenPosts 			= new ArrayList<Post>();
		pageURL 				= url;
		this.totalTime			= Converter.minToMS(totalTime);
		this.searchInterval 	= Converter.minToMS(searchInterval);
		this.searchNum 			= searchNum;

		run();
	}


	private boolean downloadPage(String url)
	{
		if (url.contains("+") || url.contains("r/all"))
		{
			isMultiReddit = true;
		}
		else
		{
			isMultiReddit = false;
		}
		boolean isNotDownloaded = true;
		int numNetworkFailures = 0;
		while (isNotDownloaded)
		{
			try
			{
				Document d = Jsoup.connect(url).get();
				htmlString = d.outerHtml();
				isNotDownloaded = false; // you just downloaded it
				System.out.println("Download successful");
				System.out.println();

//				// debug: save the file
				PrintWriter out = new PrintWriter("downloaded reddit.html");
				out.print(htmlString);
			}
			catch (Exception e)
			{
				numNetworkFailures++;
				System.out.println("network disconnect: " + numNetworkFailures + " consecutive times");
				if (numNetworkFailures == maximumConsecutiveNoDownloads)
				{
					System.out.println("maximum alowable consecutive network disconnect reached...");
					System.out.println("assuming network unavaialbe, salvaging already downloaded...");
					System.out.println();
					return false;
				}
			}
		}

		return !isNotDownloaded; // isNotDownloaded should be false @ success end -> NOT()
	}


	private void run()
	{
		int runTimes = 0;
		long currentRunTimeMS = 0;

		outerLoop: while (currentRunTimeMS < totalTime)
		{
			long beforeTime = Converter.nsToMS(System.nanoTime());

			boolean isDownloaded = downloadPage(pageURL);
			if (!isDownloaded) // network failrue
			{
				break;
			}

			if (isDownloaded)
			{
				System.out.println("Searched (" + (runTimes + 1) + ") times");
				System.out.println(Converter.msToMin(totalTime - currentRunTimeMS) + " mins remain");
				Clock.printTime();

				isFirstPage = true;
				int added = 0;
				currentSearchPos = 0;

				// keep track of how many posts the bot has searched, regardless of adding
				//		(one post is searched each loop iteration)
				int postsSearched = 0;

				while (added < searchNum && postsSearched < 26)
				{
					// if the user has requested that this bot add > 25 links
					// 		the bot will then need to move onto the next page
					//		because default reddit only shows the first 25 links
					if (postsSearched == 24 && searchNum > 25)
					{
						postsSearched = 0;
						String nextPageURL;
						if (isFirstPage)
						{
							isFirstPage = false;
							nextPageURL = findInformation(currentSearchPos,
							                              nextPageFirstTime_Begin, nextPageFirstTime_End);
						}
						else
						{
							nextPageURL = findInformation(currentSearchPos,
							                              nextPageAllOther_Begin, nextPageAllOther_End);
						}

						currentSearchPos = 0;
						boolean downloadFinished = downloadPage(nextPageURL);

						if (!downloadFinished) // network failure
						{
							break outerLoop;
						}
					}

					Post p = createPost();

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

					postsSearched++;
				}
				System.out.println("total unique posts downloaded: " + notSeenPosts.size());
			}
			runTimes++;

			long afterTime = Converter.nsToMS(System.nanoTime());
			long timeDiff = afterTime - beforeTime;

			currentRunTimeMS += searchInterval;

			// re-checking the loop condition here lets the bot skip waiting a searchInterval
			//		at the very end of total searching,
			//		-> outputs html faster by -1 searchInterval's time
			if (timeDiff < searchInterval && currentRunTimeMS < totalTime)
			{
				try
				{
					Thread.sleep(searchInterval - timeDiff);
				}
				catch (InterruptedException e) {}
			}

			System.out.println();
		}

		System.out.print("Sorting downloaded.... ");
		Collections.sort(notSeenPosts);
		System.out.println("Done!");

		writeToFile();
	}


	private Post createPost()
	{
		String score = findScore(currentSearchPos);

		String url = findInformation(currentSearchPos, url_Begin,
		                             url_End);

		String title = findInformation(currentSearchPos,
		                               title_Begin, title_End);

		String subReddit;
		if (isMultiReddit)
		{
			subReddit = findInformation(currentSearchPos,
			                            subreddit_Begin, subreddit_End);
		}
		else
		{
			subReddit = "";
		}


		// Sometimes comments can be empty, and that changes the formating on the html document
		// 		therefore, test to see if you find the comments, if its in a bad spot,
		//		parse for emptyComments
		String comments = null;
		int commentsPos = htmlString.indexOf(comments_Begin, currentSearchPos)
		                  + comments_Begin.length();
		int numCommentsPos = htmlString.indexOf(commentNum_Begin, currentSearchPos)
		                     + commentNum_Begin.length();
		if (commentsPos >= currentSearchPos && commentsPos <= numCommentsPos)
		{
			comments = findInformation(currentSearchPos,
			                           comments_Begin, comments_End);
		}
		else
		{
			comments = findInformation(currentSearchPos,
			                           emptyComments_Begin, emptyComments_End);
		}

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
//Character.isLetter()
		return new Post(prossessedScore, url, title,
		                subReddit, comments,
		                prossessedNumComments);
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


//	public static void main(String[] args)
//	{
//		if (args.length != 4)
//		{
//			System.out.println("usage: <url> <min search delay> <total searches> <posts to search each time>");
//			System.out.println("(without the < and > of course)");
//			return;
//		}
//
//		String url = args[0];
//
//		double minDelay = 0;
//		try
//		{
//			minDelay = Double.parseDouble(args[1]);
//		}
//		catch (NumberFormatException e)
//		{
//			System.out.println("cannot determin what number: (" + args[1] + ") is");
//			System.out.println("usage: <url> <min search delay> <total searches> <posts to search each time>");
//			System.out.println("(without the < and > of course)");
//			return;
//		}
//
//		int maxSearchTimes = 0;
//		try
//		{
//			maxSearchTimes = Integer.parseInt(args[2]);
//		}
//		catch (NumberFormatException e)
//		{
//			System.out.println("cannot determin what number: (" + args[2] + ") is");
//			System.out.println("usage: <url> <min search delay> <total searches> <posts to search each time>");
//			System.out.println("(without the < and > of course)");
//			return;
//		}
//
//		int searchNum = 0;
//		try
//		{
//			searchNum = Integer.parseInt(args[3]);
//		}
//		catch (NumberFormatException e)
//		{
//			System.out.println("cannot determin what number: (" + args[3] + ") is");
//			System.out.println("usage: <url> <min search delay> <total searches> <posts to search each time>");
//			System.out.println("(without the < and > of course)");
//			return;
//		}
//
//		new Downloader(url, minDelay, maxSearchTimes, searchNum);
//
//	}

}
