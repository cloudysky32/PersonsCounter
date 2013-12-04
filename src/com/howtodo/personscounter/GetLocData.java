package com.howtodo.personscounter;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.protocol.*;
import org.apache.http.util.*;
import org.json.*;

import com.google.android.maps.*;

import android.util.*;

public class GetLocData {
	private String goLocName;
	private String address;
	private String status;
	private GeoPoint myGp;
	private JSONArray jArray_LocData;
	private static final String APIKEY = "AIzaSyAWfsK44jmq4D_ZHb0YjOh2CA6m0b1nMqg";
	
	public GetLocData(String goLocName, GeoPoint myGp) {
		this.goLocName = goLocName;
		this.myGp = myGp;
		
		StringBuffer URL = new StringBuffer();
		String result = null;
		InputStream is = null;
		
		URL.append("https://maps.googleapis.com/maps/api/place/search/json?location=");
		URL.append(this.myGp.getLatitudeE6()/1E6);
		URL.append(",");
		URL.append(this.myGp.getLongitudeE6()/1E6);
		URL.append("&radius=500&types=food&name=");
		URL.append(goLocName.replace(" ", "%20"));
		URL.append("&sensor=false&key=");
		URL.append(APIKEY);

		// http post
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL.toString());
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
			
			// convert response to string
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
				StringBuilder sb = new StringBuilder();
//				sb.append(reader.readLine() + "\n");
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
			} catch (Exception e) {
				status = "Fail";
				Log.e("log_tag", "Error converting result " + e.toString());
			}
			
			try {
				JSONObject jObject = new JSONObject(result);
				jArray_LocData = jObject.getJSONArray("results");
				status = jObject.getString("status");
			} catch (JSONException e) {
				status = "Fail";
				Log.e("log_tag", "Error parsing data!! " + e.toString());
			}	
		} catch (Exception e) {
			status = "Fail";
			Log.e("log_tag", "Error in http connection " + e.toString());
		}
	}

	public JSONArray getjArray_LocData() {
		return jArray_LocData;
	}
	
	public String getStatus() {
		return status;
	}
}
