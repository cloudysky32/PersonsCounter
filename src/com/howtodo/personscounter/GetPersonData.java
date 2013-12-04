package com.howtodo.personscounter;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.protocol.*;
import org.json.*;

import android.util.*;

public class GetPersonData {
	private int currentNum;
	private int dayNum;
	private int weekNum;
	private int monthNum;
	private String qAddr;

	private static final String URL = "http://cloudysky.iptime.org/getpersondata.php";

	public GetPersonData(String qAddr) {
		this.qAddr = qAddr;
		
		String result = null;
		InputStream is = null;
		StringBuilder sb = null;

		// the year data to send
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("q_address", this.qAddr));
		
		// http post
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (Exception e) {
			Log.e("log_tag", "Error in http connection " + e.toString());
		}

		// convert response to string
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {
			Log.e("log_tag", "Error converting result " + e.toString());
		}

		// parse json data
		JSONArray jArray;
		try {
			jArray = new JSONArray(result);
			JSONObject json_data = null;
			
			for (int i = 0; i < jArray.length(); i++) {
				json_data = jArray.getJSONObject(i);
				currentNum = json_data.getInt("current_num");
				dayNum = json_data.getInt("day_num");
				weekNum = json_data.getInt("week_num");
				monthNum = json_data.getInt("month_num");
			}
		} catch (JSONException e) {
			Log.e("log_tag", "Error parsing data! " + e.toString());
		}
	}
	
	public int getCurrentNum(){
		return currentNum;
	}
	
	public int getDayNum(){
		return dayNum;
	}
	
	public int getWeekNum(){
		return weekNum;
	}
	
	public int getMonthNum(){
		return monthNum;
	}
}
