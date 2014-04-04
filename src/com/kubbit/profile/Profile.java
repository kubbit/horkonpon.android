package com.kubbit.profile;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.kubbit.utils.Utils;

public class Profile
{
	public static String getPhone(Context context)
	{
		if (!Utils.HasPermition(context, android.Manifest.permission.READ_PHONE_STATE))
			return "";

		TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

		return telephonyManager.getLine1Number();
	}

	public static String getMail(Context context)
	{
		if (!Utils.HasPermition(context, android.Manifest.permission.GET_ACCOUNTS))
			return null;

		AccountManager accountManager = AccountManager.get(context);
		Account account = Profile.getAccount(accountManager);

		if (account == null)
			return null;

		return account.name;
	}

	private static Account getAccount(AccountManager accountManager)
	{
		Account[] accounts = accountManager.getAccountsByType("com.google");

		if (accounts.length > 0)
			return accounts[0];

		return null;
	}
}
