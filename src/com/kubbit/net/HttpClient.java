package com.kubbit.net;

import android.net.Uri;
import android.util.Base64;
import android.util.Pair;

import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.kubbit.utils.Log;
import com.kubbit.utils.Utils;

public class HttpClient
{
	private final int CONNECTION_TIMEOUT = 5 * 1000;
	private final int SOCKET_TIMEOUT = 10 * 1000;

	public class RequestMethod
	{
		public static final String CONNECT = "CONNECT";
		public static final String DELETE = "DELETE";
		public static final String GET = "GET";
		public static final String HEAD = "HEAD";
		public static final String OPTIONS = "OPTIONS";
		public static final String PATCH = "PATCH";
		public static final String POST = "POST";
		public static final String PUT = "PUT";
		public static final String TRACE = "TRACE";
	}
	private final static String CHARSET = "UTF-8";

	private String url = null;
	private HttpURLConnection connection;
	private ArrayList<Pair<String, String>> params;
	private String contentType;
	private String body;

	public HttpClient(String url)
	{
		super();

		this.params = new ArrayList<>();

		this.url = url;
	}

	public void clearParams()
	{
		this.params.clear();
	}

	public void addParam(String name, String value)
	{
		this.params.add(new Pair<>(name, value));
	}

	public void addBody(String contentType, String value)
	{
		this.contentType = contentType;
		this.body = value;
	}

	private String getEncodedParams()
	{
		Uri.Builder builder = new Uri.Builder();

		for (Pair<String, String> param: this.params)
			builder.appendQueryParameter(param.first, param.second);

		return builder.build().getEncodedQuery();
	}

	private URL generateURL(String method)
	{
		try
		{
			if (method.equals(RequestMethod.GET) && this.params.size() > 0)
				return new URL(this.url + '?' + this.getEncodedParams());

			return new URL(this.url);
		}
		catch (MalformedURLException ex)
		{
			Log.error(ex.getMessage());
			return null;
		}
	}

	public String connect(String method)
	{
		return this.connect(method, null, null);
	}

	public String connect(String method, String user, String password)
	{
		String result;

		try
		{
			URL address = this.generateURL(method);

			this.connection = (HttpURLConnection)address.openConnection();

			this.connection.setRequestMethod(method);

			if (user != null || password != null)
				this.connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString((user + ":" + password).getBytes(CHARSET), Base64.NO_WRAP));

			if (method.equals(RequestMethod.POST)
			 || method.equals(RequestMethod.PUT)
			 || method.equals(RequestMethod.PATCH))
				this.connection.setDoOutput(true);

			this.connection.setConnectTimeout(CONNECTION_TIMEOUT);
			this.connection.setReadTimeout(SOCKET_TIMEOUT);

			if (this.connection.getDoOutput())
			{
				if (this.params.size() > 0)
					Utils.writeToStream(this.getEncodedParams(), this.connection.getOutputStream(), CHARSET);
				else
				{
					this.connection.setRequestProperty("Content-Type", this.contentType);
					Utils.writeToStream(this.body, this.connection.getOutputStream(), CHARSET);
				}
				this.connection.getOutputStream().close();
			}

			InputStream response;
			if (!this.isError(this.connection.getResponseCode()))
				response = this.connection.getInputStream();
			else
				response = this.connection.getErrorStream();
			result = Utils.streamToString(response, CHARSET);
			response.close();

			return result;
		}
		catch (IOException e)
		{
			Log.error(e.getMessage());
			return e.getMessage();
		}
	}

	private Boolean isError(int code)
	{
		return code >= 400;
	}
}
