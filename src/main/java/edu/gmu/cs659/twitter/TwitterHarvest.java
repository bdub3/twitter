package edu.gmu.cs659.twitter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TweetEntity;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import edu.gmu.cs659.twitter.writer.CaptureLogger;
import edu.gmu.cs659.twitter.writer.TermCapture;

public class TwitterHarvest {


	public static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat
			.dateTime();

	private static final int WOEID_USA = 23424977;

	private CaptureLogger capture;
	
	private TermCapture termCapture;
	
	private List<Tweet> tweets;

	private static final Logger logger = LoggerFactory
			.getLogger(TwitterHarvest.class);

	public static void main(String[] args) {
		TwitterHarvest harvest = new TwitterHarvest();
		try {
			harvest.grabTestData();
		} catch (TwitterException e) {
			logger.error("Failed! : ", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Failed! : ", e);
		} finally {
			harvest.capture.closeWriter();
			harvest.termCapture.closeWriter();
		}
	}

	private Twitter twitter;

	public TwitterHarvest() {

		/*  switched to properties file
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(API_KEY)
				.setOAuthConsumerSecret(API_SECRET)
				.setOAuthAccessToken(ACCESS_TOKEN)
				.setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
		*/
		TwitterFactory tf = new TwitterFactory();
		twitter = tf.getInstance();
		capture = new CaptureLogger("output_" + System.currentTimeMillis()
				+ ".csv");
		termCapture = new TermCapture("tweetTerms_" + System.currentTimeMillis()
				+ ".csv");
		tweets = new ArrayList<Tweet>();
	}

	// hmm they stole our project: http://www.hashtags.org/trending-on-twitter/

	public void grabTestData() throws TwitterException {
		Trends trends = twitter.getPlaceTrends(WOEID_USA);

		for (Trend trend : trends.getTrends()) {
			exploreTrend(trend);
		}
		
		capture.writeData(tweets);
		termCapture.writeData(tweets);
	}

	private void exploreTrend(Trend trend) throws TwitterException {
		logger.info(trend.getName());
		Query query = new Query(trend.getQuery());
		query.count(1000);
		QueryResult results = twitter.search(query);
		for (Status status : results.getTweets()) {
			processTweetFromTrend(status, trend.getName());
		}
	}

	private void processTweetFromTrend(Status status, String trendName) {
		Tweet tweet = new Tweet();
		tweets.add(tweet);

		tweet.setTweetClass(trendName);
		tweet.setTweetStatus(status.getText());
		
		tweet.addAttribute(trendName);
		tweet.addAttribute(dateTimeFormatter.print(status.getCreatedAt().getTime()));
		tweet.addAttribute(status.getCreatedAt().getTime());
		tweet.setTimeStamp(status.getCreatedAt().getTime());

		// TODO: this type of work should be post-processing collected data
		tweet.addAttribute(
				DayTimeMapper.getDayTimeMapper().getPeriod(
						status.getCreatedAt(), status.getUser().getTimeZone()));

		tweet.addAttribute(status.getSource());

		// TODO: post process to a lists of words across all tweets, treat as
		// features
		tweet.addAttribute(status.getText());
		tweet.addAttribute(status.getIsoLanguageCode());
		tweet.addAttribute(Integer.toString(status.getAccessLevel()));
		tweet.addAttribute(status.getUser().getName());
		tweet.addAttribute(status.getFavoriteCount());
		if (status.getGeoLocation() != null) {
			tweet.addAttribute(status.getGeoLocation().getLatitude());
			tweet.addAttribute(status.getGeoLocation().getLongitude());
		} else {
			tweet.addAttribute(null);
			tweet.addAttribute(null);
		}

		// TODO: lot more semantics to this object
		if(status.getPlace() != null) {
			tweet.addAttribute(status.getPlace().getFullName());
		} else {
			tweet.addAttribute(null);
		}

		tweet.addAttribute(status.getRetweetCount());

		// could be interesting... co-occurance... build graphs
		tweet.addAttribute(concatTweetEntities(status.getHashtagEntities(), "#"));
		tweet.addAttribute(concatTweetEntities(status.getMediaEntities(), "@"));
		tweet.addAttribute(
				concatTweetEntities(status.getUserMentionEntities(), "@"));
		tweet.addAttribute(status.getHashtagEntities().length);
		tweet.addAttribute(status.getMediaEntities().length);
		tweet.addAttribute(status.getUserMentionEntities().length);
	}



	private String concatTweetEntities(TweetEntity[] entities, String delimiter) {
		if (entities == null) {
			return null;
		}

		List<String> list = new ArrayList<String>();
		for (TweetEntity entity : entities) {
			list.add(entity.getText());
		}
		return StringUtils.join(list, delimiter);

	}

}
