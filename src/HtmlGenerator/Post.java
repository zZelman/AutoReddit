package HtmlGenerator;
import java.io.PrintWriter;
import java.util.Arrays;



// This class is just a plane old data field with some accessor methods
public class Post implements Comparable<Post>
{

	public int 		score;			// the net score of the post
	public String 	url;			// link to the url that the post directs to
	public String 	title;			// title of post
	public String 	subReddit;		// subreddit that this post is under
	public String 	commentsLink;	// link to the comments on this post
	public int 		numComments;	// number of comments on this post

	// file format flags
	public static final String scoreFlag 		= "score=";
	public static final String titleFlag 		= "title=";
	public static final String subRedditFlag 	= "subReddit=";
	public static final String urlFlag 			= "url=";
	public static final String commentsFlag 	= "comments=";
	public static final String numCommentsFlag 	= "numComments=";


	public Post() {}


	public Post(int score,
	            String url, String title,
	            String subReddit,
	            String commentsLink, int numComments)
	{
		this.score 			= score;
		this.url 			= url;
		this.title 			= title;
		this.subReddit 		= subReddit;
		this.commentsLink 	= commentsLink;
		this.numComments 	= numComments;
	}


	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Post)
		{
			Post p = (Post) other;

			if (this.url.equals(p.url))
			{
				return true;
			}

		}

		return false;
	}


	@Override
	public int compareTo(Post other)
	{
		if (this.score > other.score)
		{
			return -1;
		}
		else if (this.score < other.score)
		{
			return 1;
		}

		return 0;
	}


	@Override
	public String toString()
	{
		String seperator = System.getProperty("line.separator");

		String score 		= "score      : " + this.score + seperator;
		String title		= "title      : " + this.title + seperator;
		String subReddit	= "subReddit  : " + this.subReddit + seperator;
		String url			= "url        : " + this.url + seperator;
		String commentsLink = "comments   : " + this.commentsLink + seperator;
		String numComments 	= "numComments: " + this.numComments + seperator;

		return score + title + subReddit + url + commentsLink + numComments;
	}


	public void writeToFile(PrintWriter out)
	{
		String seperator = System.getProperty("line.separator");

		String score 		= scoreFlag			+ this.score + seperator;
		String title		= titleFlag			+ this.title + seperator;
		String subReddit	= subRedditFlag		+ this.subReddit + seperator;
		String url			= urlFlag 			+ this.url + seperator;
		String commentsLink = commentsFlag 		+ this.commentsLink + seperator;
		String numComments 	= numCommentsFlag 	+ this.numComments + seperator;

		out.println(score + title + subReddit + url + commentsLink + numComments);
	}


	public static void main(String[] args)
	{
		Post[] p = new Post[3];
		p[0] = new Post(2595, "http://i.imgur.com/Zw4vrjZ.jpg", "...and the infinite sadness.", "funny", "http://www.reddit.com/r/funny/comments/1p95e9/and_the_infinite_sadness/", 110);
		p[1] = new Post(1817, "http://imgur.com/gallery/vnWBMvu", "Man's best friend", "funny", "http://www.reddit.com/r/funny/comments/1p985l/mans_best_friend/", 55);
		p[2] = new Post(3527, "http://i.imgur.com/kBjqtAz.jpg", "Accidental cookie monster in Costco", "pics", "http://www.reddit.com/r/pics/comments/1p90fq/accidental_cookie_monster_in_costco/", 232);
		Arrays.sort(p);
		System.out.println(p[0]);
		System.out.println(p[0]);
	}

}
