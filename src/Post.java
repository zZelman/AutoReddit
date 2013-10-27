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


	Post() {}


	Post(int score,
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
		String score 		= "score      : " + this.score + "\n";
		String title		= "title      : " + this.title + "\n";
		String subReddit	= "subReddit  : " + this.subReddit + "\n";
		String url			= "url        : " + this.url + "\n";
		String commentsLink = "comments   : " + this.commentsLink + "\n";
		String numComments 	= "numComments: " + this.numComments + "\n";

		return score + title + subReddit + url + commentsLink + numComments;
	}


	public void writeToFile(PrintWriter out)
	{
		String score 		= "score=" + this.score + "\n";
		String title		= "title=" + this.title + "\n";
		String subReddit	= "subReddit=" + this.subReddit + "\n";
		String url			= "url=" + this.url + "\n";
		String commentsLink = "comments=" + this.commentsLink + "\n";
		String numComments 	= "numComments=" + this.numComments + "\n";

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
