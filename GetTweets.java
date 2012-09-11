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
import java.util.Calendar;

public final class GetTweets
{
    public static final String WORLDWIDE = "1";
    public static String APPID = "9F690BB7F961173D7770A833297CD1317B62A93E";

    public static void main(String[] args) throws IOException
    {
        File f = new File("24tweets.txt");
        Calendar c = Calendar.getInstance();
        if (args.length < 1)
        {
            args = new String[1];
            args[0] = WORLDWIDE;
        }//end if
        boolean done = false;
        while(!done)
        {
            try
            {
                Twitter twitter = new TwitterFactory().getInstance();
                Trends trends = twitter
                        .getLocationTrends(Integer.parseInt(args[0]));
                f.createNewFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(f, true));

                for (Trend trend : trends.getTrends())
                {
                    if( trend.getName().charAt(0) == '#')
                        continue;
                    newsSearch(trend.getName());
                    out.write(trend.getName());
                    out.write("\n");
                    String calendar_time = "" + c.getTimeInMillis();
                    out.write(calendar_time);
                    out.write("\n");
                }//end for
                out.close();
                done = true;
            } catch (TwitterException te)
            {
                te.printStackTrace();
                System.out.println("Failed to get location trends: "
                        + te.getMessage());
                done = false;
            }//end try catch
            
        }//end while
            System.exit(0);        
    }//end main
    
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
            return;
        }
        if(response == null)
        { 
            return;
        }
        if(response.getNews() == null)
        {
            return;
        }
        
        for (NewsResult result : response.getNews().getResults())
        {
            makeSnippetFile(result.getTitle(), result.getSnippet(), 
                        result.getDate(), result.getSource(), result.getUrl());
        }//end for
    }//end newsSearch
    
    public static void makeSnippetFile(String title, String snippet,
                        String date, String source, String URL)
    {
        // Filter ?query? strings
        String n_title = title.replaceAll("[^A-Za-z0-9-]", "");
        String noq_snippet = snippet.replace("?", "");
        String noq_title = title.replace("?", "");
        // create file from title
        File f = new File("news_snippets/"+n_title+".txt");
        if(f.exists())
            return;
        try
        {
            f.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            out.write(noq_title + "\n");
            out.write(source + "    " + URL + "\n");
            out.write(date + "\n");
            out.write(noq_snippet);
            out.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }//end makeSnippetFile
    
    
}
