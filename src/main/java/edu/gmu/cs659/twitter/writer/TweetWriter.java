package edu.gmu.cs659.twitter.writer;

import java.util.List;

import edu.gmu.cs659.twitter.Tweet;

public interface TweetWriter {

	public abstract void closeWriter();

	public abstract void writeData(List<Tweet> tweets);

}