package com.heroic.smscatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceiver extends BroadcastReceiver {

	public static final String SMS_EXTRA_NAME = "pdus";

	public static final String URL = "http://mysite.com/confirm.php";

	public static final String MSG_PATTERN = "^CONFIRM ([a-zA-Z0-9]{6})$";

	@Override
	public void onReceive(Context context, Intent intent) {
		// We send a request to ourselves that a person confirmed with the code
		Bundle extras = intent.getExtras();

		Pattern pattern = Pattern.compile(MSG_PATTERN);

		Pattern number_pattern = Pattern.compile("^[0-9]{10,}");

		if(extras != null) {
			// Get received SMS array
			Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

			for(int i = 0; i < smsExtra.length; ++i) {
				final SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

				Matcher match = pattern.matcher(sms.getMessageBody().toString());
				final String address = sms.getOriginatingAddress().toString().replaceAll("[^0-9]", "");
				Matcher number = number_pattern.matcher(address);

				if(number.find() && match.find()) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							postData(address, sms.getMessageBody().toString());
						}
					}).start();
					// the next line ensures that the SMS is then not processed
					// any futher.
					abortBroadcast();
				}
			}
		}
	}

	public void postData(String number, String msg) {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(URL);

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("msg", msg));
			nameValuePairs.add(new BasicNameValuePair("number", number));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			httpclient.execute(httppost);

		} catch(ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch(IOException e) {
			// TODO Auto-generated catch block
		}
	}
}
