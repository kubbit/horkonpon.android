package com.kubbit.utils;

import java.io.StringWriter;

public class Log
{
	private final static StringWriter info = new StringWriter();
	private final static StringWriter debug = new StringWriter();

	public static void info(String value)
	{
		info.append(value);

		debug(value);
	}

	public static void debug(String value)
	{
		debug.append(value);
		debug.append("\n");
	}

	public static String getInfo()
	{
		return info.toString();
	}

	public static String getDebug()
	{
		return debug.toString();
	}
}
