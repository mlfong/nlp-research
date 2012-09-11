/***
* @class:   getlocationtrends
* @desc:    [cs-4699] research
* @author:  man fong
*
***/

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import com.google.code.bing.search.client.*;
import com.google.code.bing.search.client.BingSearchClient.SearchRequestBuilder;
import com.google.code.bing.search.schema.*;
import com.google.code.bing.search.schema.news.NewsResult;
import com.google.code.bing.search.schema.web.*;


import java.util.List;
import java.io.*;

public final class GetLocationTrends
{
    public static final String WORLDWIDE = "1";
    public static String APPID = "9F690BB7F961173D7770A833297CD1317B62A93E";

    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            args = new String[1];
            args[0] = WORLDWIDE;
        }//end if
        try
        {
            Twitter twitter = new TwitterFactory().getInstance();
            Trends trends = twitter
                    .getLocationTrends(Integer.parseInt(args[0]));
            System.out.println("Showing location trends for woeid:" + args[0]);
            System.out.println("As of : " + trends.getAsOf());
            for (Trend trend : trends.getTrends())
            {
                if( trend.getName().charAt(0) == '#')
                    continue;
                System.out.println("  Trend: " + trend.getName());
                newsSearch(trend.getName());
                System.out.println("    retweets:");
                searchTweets(twitter, trend.getName());
            }//end for
            System.out.println("done.");
            System.exit(0);
        } catch (TwitterException te)
        {
            te.printStackTrace();
            System.out.println("Failed to get location trends: "
                    + te.getMessage());
            System.exit(-1);
        }//end try catch
    }//end main
    
    public static void searchTweets(Twitter twitter, String trendName)
    {
        try
        {
            QueryResult result = twitter.search(new Query(trendName));
            List<Tweet> tweets = result.getTweets();
            for (Tweet tweet : tweets)
            {
                System.out.println("      @" + tweet.getFromUser() + " - "
                        + tweet.getText().replace("\n", " "));
            }//end for
        } catch (TwitterException te)
        {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
            System.exit(-1);
        }//end try catch
    }//end searchTweets
    
    public static void newsSearch(String s)
    {
        BingSearchServiceClientFactory factory = BingSearchServiceClientFactory.newInstance();
        BingSearchClient client = factory.createBingSearchClient();

        SearchRequestBuilder builder = client.newSearchRequestBuilder();
        builder.withAppId(APPID);
        builder.withQuery(s);
        builder.withSourceType(SourceType.NEWS);
        builder.withVersion("2.0");
        builder.withMarket("en-us");
        builder.withAdultOption(AdultOption.MODERATE);
        builder.withSearchOption(SearchOption.ENABLE_HIGHLIGHTING);

        builder.withWebRequestCount(10L);
        builder.withWebRequestOffset(0L);
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_HOST_COLLAPSING);
        builder.withWebRequestSearchOption(WebSearchOption.DISABLE_QUERY_ALTERATIONS);

        SearchResponse response = null;
        try
        {
            response = client.search(builder.getResult());
        }
        catch (Exception e)
        {
            System.out.println("  No news.");
            return;
        }
        if(response == null)
        { 
            System.out.println("  No news.");
            return;
        }
        if(response.getNews() == null)
        {
            System.out.println("  No news.");
            return;
        }
        


        for (NewsResult result : response.getNews().getResults())
        {
            System.out.println("  News: " + result.getTitle());
            System.out.println("    Written at: " + result.getDate());
            System.out.println("    Source and URL: " + result.getSource() + " - " + result.getUrl());
            System.out.println("      " + result.getSnippet());
            makeSnippetFile(result.getTitle(), result.getSnippet());
        }//end for
    }//end newsSearch
    
    public static void makeSnippetFile(String title, String snippet)
    {
        // Filter ?query? strings
        title = title.replaceAll("[^A-Za-z0-9-]", "");
        snippet = snippet.replace("?", "");
        // create file from title
        File f = new File("news_snippets/"+title+".txt");
        if(f.exists())
            return;
        try
        {
            f.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            out.write(snippet);
            out.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }//end makeSnippetFile
    
}
