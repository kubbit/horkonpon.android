package com.kubbit.horkonpon;

public class Constants
{
	public final static String API_URL = "https://api.horkonpon.com/v3";
	public final static String DISCLAIMER_URL = "https://r.horkonpon.com/%s/privacy/";

	public final static String BROADCAST_KEY = "com.kubbit.horkonpon.BROADCAST";

	public static final String IMAGE_EXTENSION = "jpg";
	public static final int IMAGE_QUALITY = 90;
	public static final int IMAGE_MIN_SIZE = 600;
	public static final int THUMBNAIL_QUALITY = 50;
	public static final int THUMBNAIL_MIN_SIZE = 125;

	public class IssueStatus
	{
		static final int NEW = 0;
		static final int SENT = 1;
		static final int DISCARDED = 2;
		static final int FIXED = 3;
	}

	public class AppVersion
	{
		static final int V1_0 = 1;
		static final int V1_2 = 2;
		static final int V2_0_RC1 = 3;
		static final int V2_0_RC2 = 4;
		static final int V2_0_RC3 = 5;
		static final int V2_0_RC4 = 6;
		static final int V2_0_RC5 = 7;
		static final int V2_0_RC6 = 8;
		static final int V2_0 = 9;
	}

	public class ActivityResultCode
	{
		public static final int TAKE_PICTURE = 2;
	}

	public class AppPermission
	{
		static final int LOCATION = 0;
	}
}
