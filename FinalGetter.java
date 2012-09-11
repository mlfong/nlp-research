/***
* @class:   finalgetter
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

public final class FinalGetter
{
    public static final String WORLDWIDE = "1";
    public static String APPID = "9F690BB7F961173D7770A833297CD1317B62A93E";

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1)
        {
            args = new String[1];
            args[0] = WORLDWIDE;
        }//end if
        try
        {
            new File("tweets_dead/").mkdir();
            
            String theTrend = "";
            String theRetweets = "";
            String theNews = "";
            
            Twitter twitter = new TwitterFactory().getInstance();
            Trends trends = twitter
                    .getLocationTrends(Integer.parseInt(args[0]));
            for (Trend trend : trends.getTrends())
            {
                if( trend.getName().charAt(0) == '#')
                    continue;
    
                int k = 1;
                BufferedWriter bw_keep;
                while (new File("tweets_dead/" + k + ".txt").exists())
                    k++;
                bw_keep = new BufferedWriter(new FileWriter("tweets_dead/" + k + ".txt"));
                    
                theTrend = trend.getName();
                theRetweets = searchTweets(twitter, trend.getName());
                theNews = newsSearch(trend.getName());

                bw_keep.write(theTrend + "\n");
                bw_keep.write(theRetweets + "\n");
                bw_keep.write(theNews + "\n");
                
                bw_keep.close();                
            }//end for
            
            System.exit(0);
        } catch (TwitterException te)
        {
            te.printStackTrace();
            System.out.println("Failed to get location trends: "
                    + te.getMessage());
            System.exit(0);
        }//end try catch
    }//end main
    
    public static String searchTweets(Twitter twitter, String trendName)
    {
        try
        {
            String retweets = "";
            QueryResult result = twitter.search(new Query(trendName));
            List<Tweet> tweets = result.getTweets();
            for (Tweet tweet : tweets)
            {
                retweets += ("@" + tweet.getFromUser() + " - "
                        + tweet.getText().replace("\n", " "));
            }//end for
            return retweets;
        } catch (TwitterException te)
        {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
            return "No tweets";
        }//end try catch
    }//end searchTweets
    
    public static String newsSearch(String s)
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

        String newsStr = "";
        SearchResponse response = null;
        try
        {
            response = client.search(builder.getResult());
        } catch (Exception e)
        {
            newsStr = "No news";
        }
        if (response == null)
        {
            newsStr = "No news";
        }
        else
        {
            if (response.getNews() == null)
            {
                newsStr = "No news";
            }
        }
        if (!newsStr.equals("No news"))
        {
            for (NewsResult result1 : response.getNews().getResults())
            {
                newsStr += result1.getTitle() + " " + result1.getSnippet() + " ";
            }// end for
        }
        newsStr = newsStr.replaceAll("\n", " ");
        newsStr = newsStr.replaceAll("[^\\p{ASCII}]", "");

        return newsStr;
    }//end newsSearch
    
}
