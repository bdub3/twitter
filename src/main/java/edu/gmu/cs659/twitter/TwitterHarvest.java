package edu.gmu.cs659.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gmu.cs659.twitter.writer.CaptureLogger;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TweetEntity;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class TwitterHarvest {


	public static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat
			.dateTime();

	private static final int WOEID_USA = 23424977;

	private CaptureLogger capture;

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
				+ ".txt");

	}

	// hmm they stole our project: http://www.hashtags.org/trending-on-twitter/

	public void grabTestData() throws TwitterException {
		Trends trends = twitter.getPlaceTrends(WOEID_USA);

		capture.writeRow(Arrays.asList("trend", "time", "dayTimePeriod",
				"source", "text", "lang", "accessLevel", "user", "favCount",
				"lat", "lng", "place", "retweetCount", "hashtaglist",
				"mediaEntries", "userMentions"));
		for (Trend trend : trends.getTrends()) {
			exploreTrend(trend);
		}

	}

	private void exploreTrend(Trend trend) throws TwitterException {
		logger.info(trend.getName());
		Query query = new Query(trend.getQuery());
		QueryResult results = twitter.search(query);
		for (Status status : results.getTweets()) {
			processTweetFromTrend(status, trend.getName());
		}
	}

	private void processTweetFromTrend(Status status, String trendName) {
		List<Object> list = new ArrayList<Object>();
		addToList(list, trendName);
		addToList(list, status.getCreatedAt().getTime());

		// TODO: this type of work should be post-processing collected data
		addToList(
				list,
				DayTimeMapper.getDayTimeMapper().getPeriod(
						status.getCreatedAt(), status.getUser().getTimeZone()));

		addToList(list, status.getSource());

		// TODO: post process to a lists of words across all tweets, treat as
		// features
		addToList(list, status.getText());
		addToList(list, status.getIsoLanguageCode());
		addToList(list, Integer.toString(status.getAccessLevel()));
		addToList(list, status.getUser().getName());
		if (status.getGeoLocation() != null) {
			addToList(list, status.getGeoLocation().getLatitude());
			addToList(list, status.getGeoLocation().getLongitude());
		} else {
			addToList(list, null);
			addToList(list, null);
		}

		// TODO: lot more semantics to this object
		if(status.getPlace() != null) {
			addToList(list, status.getPlace().getFullName());
		} else {
			addToList(list, null);
		}

		addToList(list, status.getRetweetCount());

		// could be interesting... co-occurance... build graphs
		addToList(list, concatTweetEntities(status.getHashtagEntities(), "#"));
		addToList(list, concatTweetEntities(status.getMediaEntities(), "@"));
		addToList(list,
				concatTweetEntities(status.getUserMentionEntities(), "@"));

		capture.writeRow(list);
	}

	/**
	 * null safe add
	 * 
	 * @param list
	 *            list to add to
	 * @param o
	 *            item to add, or "" if item is null
	 */
	private void addToList(List<Object> list, Object o) {
		if (o == null) {
			list.add("");
		} else {
			list.add(o);
		}
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
