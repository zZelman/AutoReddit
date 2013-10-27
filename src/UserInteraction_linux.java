import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;



// * this class becomes an object after reddit has been searched many times
// * this interacts with the user and allows them to do different things with the links found
public class UserInteraction_linux
{

	// The end product of searching reddit,
	//		a sorted list of objects that the user will then step through
	//		to decide what they want to do with the links
	private ArrayList<Post> sortedPosts;

	// A growing arraylist of posts that the user choose to see
	private ArrayList<Post> seenPosts;


	UserInteraction_linux() {}


	UserInteraction_linux(ArrayList<Post> sortedPosts)
	{
		this.sortedPosts = sortedPosts;
		seenPosts = new ArrayList<Post>();

		interactWithUser();
	}


	private void interactWithUser()
	{
		System.out.println("----------------------------------------");
		System.out.println();

		System.out.println("Welcome!");
		System.out.println("I have searched Reddit all day for you, I hope you like what I found.");
		System.out.println("Here is your sorted content:");
		System.out.println();

		Scanner scan = new Scanner(System.in);
		for (int i = 0; i < sortedPosts.size(); i++)
		{
			Post p = sortedPosts.get(i);
			displayPost(p);
			chooseAction(scan, p);

			System.out.println();
		}

	}


	// dispoays information contained in the post within sortedPosts at index
	private void displayPost(Post p)
	{
		System.out.println("Score     : " + p.score);
		System.out.println("SubReddit : " + p.subReddit);
		System.out.println("Title     : " + p.title);
	}


	private void chooseAction(Scanner scan, Post p)
	{
		System.out.print("What do? (a and/or s) ");
		String command = scan.next();
		System.out.println();

		String openLinkCommand = "s";
		String openCommentsCommand = "a";

		boolean openLink = false;
		boolean openComments = false;

		if (command.contains(openLinkCommand))
		{
			openLink = true;
		}

		if (command.contains(openCommentsCommand))
		{
			openComments = true;
		}

		if (openLink && openComments)
		{
			openPage(p.url);
			openPage(p.commentsLink);
		}
		else if (!openLink && openComments)
		{
			openPage(p.commentsLink);
		}
		else if (openLink && !openComments)
		{
			openPage(p.url);
		}
		else if (!openLink && !openComments)
		{
			System.out.println("You did not enter any correct commands");
		}
	}

	private void openPage(String url)
	{
		Runtime r = Runtime.getRuntime();

		try
		{
			r.exec("firefox -new-tab " + url);
		}
		catch (IOException e)
		{
			System.out.println("I could not open page: " + url);
			System.out.println("Sorry about that, maybe you try?");
		}
	}
}
