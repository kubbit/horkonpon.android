package com.kubbit.utils;

import android.os.CountDownTimer;

public class Timer
{
	private final CountDownTimer timer;

	public Finish onFinish;
	public interface Finish
	{
		void onTimerFinish(Object sender);
	}

	public Timer(long milliseconds)
	{
		this.timer = new CountDownTimer(milliseconds, milliseconds)
		{
			@Override
			public void onTick(long millisUntilFinished)
			{
			}

			@Override
			public void onFinish()
			{
				timer_onFinish();
			}
		};
	}

	public void start()
	{
		this.timer.start();
	}

	public void stop()
	{
		this.timer.cancel();
	}

	public void reset()
	{
		this.stop();
		this.start();
	}

	private void timer_onFinish()
	{
		if (this.onFinish != null)
			onFinish.onTimerFinish(this);
	}
}
