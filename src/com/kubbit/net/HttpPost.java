package com.kubbit.net;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.kubbit.horkonpon.R;
import com.kubbit.utils.Log;

public class HttpPost extends AsyncTask<NameValuePair, Void, String>
{
	private final int TIMEOUT = 30 * 1000;

	public httpPostResult onResult;
	private final HttpParams httpParams;
	private final HttpClient httpClient;
	private final org.apache.http.client.methods.HttpPost post;
	private final Context context;

	public interface httpPostResult
	{
		void onHttpPostResult(String result);
	}

	public HttpPost(Context context, String url)
	{
		super();

		this.context = context;

		this.httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);

		this.httpClient = new DefaultHttpClient(httpParams);
		this.httpClient.getParams().setParameter("http.socket.timeout", TIMEOUT);

		this.post = new org.apache.http.client.methods.HttpPost(url);
	}

	@Override
	protected String doInBackground(NameValuePair...params)
	{
		try
		{
			UrlEncodedFormEntity ent = new UrlEncodedFormEntity(Arrays.asList(params), HTTP.UTF_8);
			post.setEntity(ent);

			HttpResponse responsePOST = httpClient.execute(post);
			HttpEntity resEntity = responsePOST.getEntity();

			if (resEntity != null)
				return EntityUtils.toString(resEntity, HTTP.UTF_8);
		}
		catch (IOException e)
		{
			Log.debug(e.getMessage());
		}
		catch (ParseException e)
		{
			Log.debug(e.getMessage());
		}
		catch (Exception e)
		{
			Log.debug(e.getMessage());
		}

		return this.context.getString(R.string.error_http);
	}

	@Override
	protected void onPostExecute(String result)
	{
		if (this.onResult != null)
			onResult.onHttpPostResult(result);
	}
}
