package HtmlGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import Downloader.Post;


// This class produces a html file from the downloaded and sorted posts from Downloader
public class HtmlGenerator
{
	private ArrayList<Post> sortedPosts;

	public static final String generatedHtmlName = "AutoReddit.html";
	public static final String tabLength = "    ";

	HtmlGenerator(ArrayList<Post> sortedPosts)
	{
		this.sortedPosts = sortedPosts;
		Collections.sort(this.sortedPosts);

		generateHtml();
	}

	HtmlGenerator(String fileName)
	{
		this.sortedPosts = new ArrayList<Post>();

		loadFile(fileName);
		Collections.sort(sortedPosts);

		generateHtml();
	}

	private void generateHtml()
	{
		File f = new File(generatedHtmlName);
		if (!f.exists())
		{
			try
			{
				f.createNewFile();
			}
			catch (IOException e)
			{
				System.out.println("Could not create: " + generatedHtmlName);
				return;
			}
		}
		PrintWriter out = null;
		try
		{
			out = new PrintWriter(generatedHtmlName);
		}
		catch (FileNotFoundException e) {}

		generateHtml_start(out);

		for (int i = 0; i < sortedPosts.size(); i++)
		{
			generateHtml_post(out, sortedPosts.get(i));
		}

		generateHtml_end(out);

		out.close();
	}


	// adds the constant START of the html to the target html file in 'out'
	private void generateHtml_start(PrintWriter out)
	{
		out.println("<html>");
		out.println(tabLength + "<body>");
		out.println();
		out.println(tabLength + tabLength + "<h1>AutoReddit</h1>");
		out.println();
		out.println(tabLength + tabLength + "<ol>");
	}


	// adds a Post with html formated information to the document targeted by 'put'
	private void generateHtml_post(PrintWriter out, Post p)
	{
		String indent = tabLength + tabLength + tabLength;
		out.println(indent + "<li>");
		out.println(indent + tabLength + p.score + " - " + p.subReddit);
		out.println(indent + tabLength + "<br/><a href=\"" + p.url + "\">" + p.title + "</a>");
		out.println(indent + tabLength + "<br/>" + p.numComments + " <a href=\"" + p.commentsLink + "\">Comments</a>");
		out.println(indent + "</li><br/>");
		out.println();
	}


	// adds the constant END of the html to the target html file in 'out'
	private void generateHtml_end(PrintWriter out)
	{
		out.println(tabLength + tabLength + "</ol>");
		out.println();
		out.println(tabLength + "</body>");
		out.println("</html>");

	}


	// loads the file into an array list of posts
	private void loadFile(String fileName)
	{
		Scanner s = null;
		try
		{
			s = new Scanner(new File(fileName));
		}
		catch (FileNotFoundException e)
		{
			System.out.println("File not found: " + fileName);
			System.exit(-1);
		}

		while (s.hasNext())
		{
			String score = null;
			String title = null;
			String subReddit = null;
			String url = null;
			String comments = null;
			String numComments = null;

			for (int i = 0; i < 6; i++)
			{
				String line = s.nextLine();
				if (line.contains(Post.scoreFlag))
				{
					score = line.substring(Post.scoreFlag.length(), line.length());
				}
				else if (line.contains(Post.titleFlag))
				{
					title = line.substring(Post.titleFlag.length(), line.length());
				}
				else if (line.contains(Post.subRedditFlag))
				{
					subReddit = line.substring(Post.subRedditFlag.length(), line.length());
				}
				else if (line.contains(Post.urlFlag))
				{
					url = line.substring(Post.urlFlag.length(), line.length());
				}
				else if (line.contains(Post.commentsFlag))
				{
					comments = line.substring(Post.commentsFlag.length(), line.length());
				}
				else if (line.contains(Post.numCommentsFlag))
				{
					numComments = line.substring(Post.numCommentsFlag.length(), line.length());
				}
			}

			if (score 			== null ||
			        title 		== null ||
			        subReddit 	== null ||
			        url 		== null ||
			        comments 	== null ||
			        numComments == null)
			{
				assert(false);
				continue;
			}

			Post p = null;
			try
			{
				p = new Post(Integer.parseInt(score), url, title,
				             subReddit, comments,
				             Integer.parseInt(numComments));
			}
			catch (NumberFormatException e)
			{
				continue;
			}
			sortedPosts.add(p);

			s.nextLine(); // consume the space between the two posts in file
		}
	}


	public void printArrayList()
	{
		for (int i = 0; i < sortedPosts.size(); i++)
		{
			System.out.println(sortedPosts.get(i));
		}
	}


	public static void main(String[] args)
	{
		new HtmlGenerator(Downloader.Downloader.fileOutputName);
	}

}