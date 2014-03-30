package edu.gmu.cs659.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.lang.StringUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TweetEntity;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import edu.gmu.cs659.twitter.writer.CaptureLogger;
import edu.gmu.cs659.twitter.writer.TermCapture;

public class TwitterHarvest {


	public static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat
			.dateTime();

	private static final int WOEID_USA = 23424977;

	
	private List<Tweet> tweets;

	private static final String STREAMING = "s";
	private static final String STREAMING_TIMEOUT = "t";

	// default values
	static boolean doStreaming = false;
	static long timeToRun = 60;

	private static final Logger logger = LoggerFactory
			.getLogger(TwitterHarvest.class);

	public static void main(String[] args) {
		TwitterHarvest harvest = new TwitterHarvest();

		parseCommandLine(args);

		if(doStreaming) {
			harvest.captureStreamingSample();
		} else {
			harvest.extractTrends();
		}
	}

	private static void parseCommandLine(String[] args) {
		Parser parser = new BasicParser();
		Options options = new Options();
		options.addOption(STREAMING, false, "Set to run in streaming mode");
		options.addOption(STREAMING_TIMEOUT, true, "Time to run streaming, in seconds.  Only applies in streaming mode");

		try {
			CommandLine commands = parser.parse(options, args);

			if(commands.hasOption(STREAMING)) {
				doStreaming = true;
				if(commands.hasOption(STREAMING_TIMEOUT)) {
					try {
						timeToRun = Integer.parseInt(commands.getOptionValue(STREAMING_TIMEOUT));
						if(timeToRun < 1) {
							logger.warn("Must have a positive time to run, using default value", 60);
							
						}
					} catch (NumberFormatException e) {
						logger.warn("Failed to parse timeout: {}.  Using default value", commands.getOptionValue(STREAMING_TIMEOUT));
					}
				}
			}
		} catch (ParseException e) {
			logger.warn("Failed to parse options: {}.  Using default values", options);
		}
	}


	private Twitter twitter;
	private TwitterStream twitterStream;

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

		tweets = new ArrayList<Tweet>();
	}

	private void extractTrends() {
		CaptureLogger capture = new CaptureLogger("output_" + System.currentTimeMillis()
				+ ".csv");
		TermCapture termCapture = new TermCapture("tweetTerms_" + System.currentTimeMillis()
				+ ".csv");
		try {
			grabTestData(capture, termCapture);
		} catch (TwitterException e) {
			logger.error("Failed! : ", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Failed! : ", e);
		} finally {
			capture.closeWriter();
			termCapture.closeWriter();
		}
	}


	public void captureStreamingSample() {
		TwitterStreamFactory streamFactory = new TwitterStreamFactory();
		twitterStream = streamFactory.getInstance();
		CaptureLogger streamCapture = new CaptureLogger("streamSample_"
				+ System.currentTimeMillis() + ".csv");
		streamCapture.writeRow(Tweet.FIELDS);

		twitterStream.addListener(new StreamStatusListener(streamCapture));
		twitterStream.sample();
		
		try {
			Thread.sleep(timeToRun * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		twitterStream.cleanUp();
		streamCapture.closeWriter();
	}
	
	// hmm they stole our project: http://www.hashtags.org/trending-on-twitter/
		
	public void grabTestData(CaptureLogger capture, TermCapture termCapture)
			throws TwitterException {
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

	private Tweet processTweet(Status status) {
		return processTweetFromTrend(status, null); 
	}
	
	private Tweet processTweetFromTrend(Status status, String trendName) {
		Tweet tweet = new Tweet();
		tweets.add(tweet);

		tweet.setTweetClass(trendName);
		tweet.setTweetStatus(status.getText());
		
		tweet.addAttribute(trendName, true);
		tweet.addAttribute(dateTimeFormatter.print(status.getCreatedAt().getTime()));
		tweet.addAttribute(status.getCreatedAt().getTime());
		tweet.setTimeStamp(status.getCreatedAt().getTime());

		// TODO: this type of work should be post-processing collected data
		TimeZone tz = TimeZone.getDefault();
		String[] tzIds = TimeZone.getAvailableIDs(status.getUser().getUtcOffset() * 1000);
		for(String id : tzIds) {
			tz = TimeZone.getTimeZone(id);
			if(tz.getRawOffset() != 0) {
				break;  // found something
			}
		}
		tweet.addAttribute(DayTimeMapper.getDayTimeMapper().getPeriod(
				status.getCreatedAt(), tz));
		tweet.addAttribute(status.getUser().getTimeZone());
		tweet.addAttribute(status.getUser().getLocation(), true);

		tweet.addAttribute(status.getSource(), true);

		// TODO: post process to a lists of words across all tweets, treat as
		// features
		tweet.addAttribute(status.getText(), true);
		tweet.addAttribute(status.getLang());
		tweet.addAttribute(status.getAccessLevel());
		tweet.addAttribute(status.getUser().getName(), true);
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
			tweet.addAttribute(status.getPlace().getFullName(), true);
		} else {
			tweet.addAttribute(null);
		}

		tweet.addAttribute(status.getRetweetCount());

		// could be interesting... co-occurance... build graphs
		tweet.addAttribute(concatTweetEntities(status.getHashtagEntities(), " "), true);
		tweet.addAttribute(concatTweetEntities(status.getMediaEntities(), " "), true);
		tweet.addAttribute(
				concatTweetEntities(status.getUserMentionEntities(), " "), true);
		tweet.addAttribute(status.getHashtagEntities().length);
		tweet.addAttribute(status.getMediaEntities().length);
		tweet.addAttribute(status.getUserMentionEntities().length);
		
		return tweet;
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

    public class StreamStatusListener implements StatusListener {
    	
    	private CaptureLogger capture;
    	
    	public StreamStatusListener(CaptureLogger capture) {
    		this.capture = capture;
    	}
    	
        public void onStatus(Status status) {
        	Tweet tweet = processTweet(status);
        	capture.writeRow(tweet.getAttributes());
        }

        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            logger.debug("StatusDeletionNotice: {}", statusDeletionNotice);        	
        }
        
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            logger.debug("Track Limitation Notice: {}", numberOfLimitedStatuses);
        }

        public void onException(Exception ex) {
            logger.error("Exception in Stream Listener", ex);
        }
		public void onScrubGeo(long arg0, long arg1) {
            logger.debug("Scrub Geo: {}, {}", arg0, arg1);
		}
		public void onStallWarning(StallWarning arg0) {
		}
    };

}
