package me.openphoto.android.app.twitter;

import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.Twitter;
import android.content.Context;

/**
 * The class which provides access to the Twitter api
 * 
 * @author Eugene Popovich
 * @version
 *          10.10.2012
 *          <br>- created
 */
public class TwitterProvider
{
	private Twitter twitter;
	private CommonsHttpOAuthConsumer consumer;
	private OAuthProvider provider;
	static TwitterProvider instance = new TwitterProvider();

	static TwitterProvider getInstance()
	{
		return instance;
	}
	public static Twitter getTwitter(Context context)
	{
		TwitterProvider provider = getInstance();
		if (provider.getTwitter() == null)
		{
			provider.setTwitter(TwitterUtils.instantiateTwitter(context));
		}
		return provider.getTwitter();
	}

	Twitter getTwitter()
	{
		return twitter;
	}

	void setTwitter(Twitter twitter)
	{
		this.twitter = twitter;
	}

	CommonsHttpOAuthConsumer getConsumer()
	{
		return consumer;
	}

	void setConsumer(CommonsHttpOAuthConsumer consumer)
	{
		this.consumer = consumer;
	}

	OAuthProvider getProvider()
	{
		return provider;
	}

	void setProvider(OAuthProvider provider)
	{
		this.provider = provider;
	}

}
