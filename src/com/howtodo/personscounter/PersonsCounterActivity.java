package com.howtodo.personscounter;

import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.logging.*;

import org.json.*;

import com.google.android.maps.*;
import com.google.android.maps.MapView.LayoutParams;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.location.*;
import android.os.*;
import android.os.Handler;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;

public class PersonsCounterActivity extends MapActivity {
	private MyCustomMapView mapView;
	private MyLocationOverlay myLocationOverlay;
	private GeoPoint myLoc;
	
	private int[] locLatitude;
	private int[] locLongitude;
	
	private ArrayList<String> locAddress = new ArrayList<String>();
	private ArrayList<String> locName = new ArrayList<String>();
	private ArrayList<String> rDataList = new ArrayList<String>();
	private final ArrayList<String> FAVORITE = new ArrayList<String>();
	private ProgressDialog loagindDialog;
	private String FILE_NAME = "favorite.txt";

	private Boolean isFinish = false;
	private Timer timer = new Timer();
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);

		final CustomAutoComplete searchBar = (CustomAutoComplete) findViewById(R.id.searchBar);
		final Button searchButton = (Button) findViewById(R.id.searchButton);
		final ToggleButton favorTb = (ToggleButton) this.findViewById(R.id.favorite);
		
		mapView = (MyCustomMapView) findViewById(R.id.map);

		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
		mapView.setBuiltInZoomControls(true);
		myLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapView.getController().setZoom(16);
				mapView.getController().animateTo(myLocationOverlay.getMyLocation());
				myLoc = myLocationOverlay.getMyLocation();
			}
		});
		getFavorite();
		
        mapView.setOnLongpressListener(new MyCustomMapView.OnLongpressListener() {        
			public void onLongpress(final MapView view, final GeoPoint longpressLocation) {           
				runOnUiThread(new Runnable() {            
					public void run() {
						myLoc = new GeoPoint(longpressLocation.getLatitudeE6(), longpressLocation.getLongitudeE6());
						setIcon(longpressLocation.getLatitudeE6(), longpressLocation.getLongitudeE6(), true);
						Toast.makeText(getApplicationContext(), "현재 위치가 재설정 되었습니다" , Toast.LENGTH_LONG).show();
					}
				});
			}
		});
               
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {	
        	@Override            
        	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {               
        		if(actionId == EditorInfo.IME_ACTION_SEARCH){                    
//        			searchButton.callOnClick();
        			searchButton.performClick();
        			return true;
        		}                
        	return false;            
        	}        
        });

        searchBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final android.view.View v, final int position, long id) {
				Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());

				List<Address> adds;
				String regularExpression = "([^,]*)";
				try {
					adds = gc.getFromLocationName(FAVORITE.get(position).replaceFirst(regularExpression, ""), 1);
					myLoc = new GeoPoint((int)(adds.get(0).getLatitude()*1E6), (int)(adds.get(0).getLongitude()*1E6));
//					searchButton.callOnClick();
					searchButton.performClick();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		searchBar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchBar.showDropDown();
			}
		});

		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				SlidingDrawer drawerR = (SlidingDrawer) findViewById(R.id.slidingdrawerR);
				
				drawerR.close();
				if(searchBar.getText().length() == 0){
					Toast.makeText(getApplicationContext(), "검색할 장소를 입력하세요", Toast.LENGTH_LONG).show();
				} else {
					goLoc(searchBar.getText().toString(), myLoc);
					imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
				}
			}
		});
		
		favorTb.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				if (favorTb.isChecked()) {
					favorTb.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_star_big_on));
					addFavorite();
				} else {
					favorTb.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_star_big_off));
					deleteFavorite();
				}
			}
		});
	}

	public class MyListAdapter extends ArrayAdapter<String> implements OnClickListener {

		private ArrayList<String> items;
		private int textViewResourceId;
		private int rowId;
		private boolean clickable;

		public MyListAdapter(Context context, int textViewResourceId, int rowId, ArrayList<String> items, boolean clickable) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.textViewResourceId = textViewResourceId;
			this.rowId = rowId;
			this.clickable = clickable;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(textViewResourceId, null);
			}

			String myData = items.get(position);
			if (myData != null) {
				TextView addrList = (TextView) v.findViewById(rowId);
				if (addrList != null) {
					addrList.setText(myData);
					// put the id to identify the item clicked
					addrList.setTag(position);
					addrList.setOnClickListener(this);
				}
			}
			return v;
		}

		@Override
		public void onClick(View v) {
			if (clickable) {
				Log.d("Sample", "Clicked on tag: " + v.getTag());
				int pos = Integer.parseInt(v.getTag().toString());

				// Do something here, like start new activity.
				try {
					TextView selected = (TextView) findViewById(R.id.selected);
					SlidingDrawer drawerR = (SlidingDrawer) findViewById(R.id.slidingdrawerR);
					GeoPoint gp = new GeoPoint(locLatitude[pos] - 7000, locLongitude[pos]);

					mapView.getController().setZoom(16);
					mapView.getController().animateTo(gp);

					selected.setText(items.get(pos) + ", " + locAddress.get(pos));
					isFavorite(items.get(pos) + ", " + locAddress.get(pos));
					getPersonData(locAddress.get(pos));
					
					if (drawerR.isOpened()) {
						drawerR.animateClose();
					} else {
						drawerR.animateOpen();
					}
				} catch (Exception e) {
					return;
				}
			} else {
				return;
			}
		}
	}
	
	public void goLoc(final String goLocName, final GeoPoint myGp) {
		final Geocoder gc = new Geocoder(this, Locale.getDefault());
		final CustomAutoComplete searchBar = (CustomAutoComplete) findViewById(R.id.searchBar);
		loagindDialog = ProgressDialog.show(this, "Searching", "Please wait...", true, false);

		Thread thread = new Thread(new Runnable() {
			public void run() {
				final GetLocData getLocData = new GetLocData(goLocName, myGp);
				final JSONArray jArray_LocData = getLocData.getjArray_LocData();
	
				if (!getLocData.getStatus().equals("OK")) {
					handler.post(new Runnable() {
						public void run() {
							SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingdrawer);
							Toast.makeText(getApplicationContext(),	goLocName + "을(를) 찾을 수 없습니다", Toast.LENGTH_LONG).show();
							drawer.close();
							searchBar.showDropDown();
						}
					});
					handler.sendEmptyMessage(0);
					return;
				}

				handler.post(new Runnable() {
					public void run() {
						try {						
							ListView mListView = (ListView) findViewById(R.id.addslist);
							SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingdrawer);
							
							locName.clear();
							locAddress.clear();
							locLatitude = new int[20];
							locLongitude = new int[20];
							JSONObject json_data = null;
							
							for (int i = 0; i < jArray_LocData.length(); i++) {
								json_data = jArray_LocData.getJSONObject(i);
								locAddress.add(json_data.getString("vicinity"));
								locName.add(json_data.getString("name"));

								locLatitude[i] = (int) (json_data.getJSONObject("geometry").getJSONObject("location").getDouble("lat")*1E6);
								locLongitude[i] = (int) (json_data.getJSONObject("geometry").getJSONObject("location").getDouble("lng")*1E6);
							}
							
							int latitude = locLatitude[0] - 7000;
							int longitude = locLongitude[0];
	
							GeoPoint gp = new GeoPoint(latitude, longitude);
	
							mapView.getController().setZoom(16);
							mapView.getController().setCenter(gp);
							mapView.getController().animateTo(gp);
	
							mapView.removeAllViews();
							for (int i = 0; i < jArray_LocData.length(); i++) {
								setIcon(locLatitude[i],	locLongitude[i], false);
							}
	
							if (!drawer.isOpened()) {
								drawer.animateOpen();
							}
	
							mListView.setAdapter(new MyListAdapter(getApplicationContext(), R.layout.row, R.id.address, locName, true));
						} catch (JSONException e) {
							Log.e("log_tag", "Error parsing data " + e.toString());
						}
					}
				});
				handler.sendEmptyMessage(0);
			}
		});
		thread.start();
	}

	public void getPersonData(String address) {
		GetPersonData gpd = new GetPersonData(address);
		rDataList.clear();

		if (gpd.getCurrentNum() == 0 && gpd.getWeekNum() == 0 && gpd.getMonthNum() == 0) {
			Toast.makeText(getApplicationContext(), "등록되지 않은 장소입니다.",	Toast.LENGTH_LONG).show();
		} else {
			rDataList.add(0, "현재인원 : " + gpd.getCurrentNum());
			rDataList.add(1, "주간평균 : " + gpd.getWeekNum());
			rDataList.add(2, "월간평균 : " + gpd.getMonthNum());
		}
		ListView rListView = (ListView) findViewById(R.id.resultlist);
		rListView.setAdapter(new MyListAdapter(this, R.layout.result_row, R.id.person_data, rDataList, false));
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			loagindDialog.dismiss();
		}
	};

	public void setIcon(int latitude, int longitude, boolean setMyloc) {
		GeoPoint gp = new GeoPoint(latitude, longitude);
		MapView.LayoutParams mapParams = new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, gp,	MapView.LayoutParams.CENTER);
		ImageView iv = new ImageView(getApplicationContext());
		
		if(setMyloc) {
			mapView.removeAllViews();
			iv.setImageResource(R.drawable.set_my_loc);
		} else {
			iv.setImageResource(R.drawable.pin);
		}

		mapView.addView(iv, mapParams);

		iv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingdrawer);
				drawer.animateToggle();
			}
		});
	}
	
	public void getFavorite() {
		BufferedReader br = null;
		CustomAutoComplete searchBar = (CustomAutoComplete) findViewById(R.id.searchBar);

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_item, FAVORITE);
		searchBar.setAdapter(arrayAdapter);

		try {
			br = new BufferedReader(new InputStreamReader(openFileInput("FILE_NAME")));

			FAVORITE.clear();
			String str = null;

			while ((str = br.readLine()) != null) {
				FAVORITE.add(str);
			}
		} catch (FileNotFoundException fn) {
			setFavorite();
		} catch (Exception e) {
			try {
				br.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void setFavorite() {
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter(openFileOutput("FILE_NAME", MODE_PRIVATE));
			for (int i = 0; i < FAVORITE.size(); i++) {
				osw.write(FAVORITE.get(i).toString() + "\n");
			}
		} catch (Exception e) {
			Toast.makeText(this, "파일저장실패", Toast.LENGTH_LONG).show();
		} finally {
			try {
				osw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void isFavorite(String selected) {
		ToggleButton favorTb = (ToggleButton) this.findViewById(R.id.favorite);

		if (FAVORITE.contains(selected)) {
			favorTb.setChecked(true);
			favorTb.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_star_big_on));
		} else {
			favorTb.setChecked(false);
			favorTb.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_star_big_off));
		}
	}

	public void addFavorite() {
		TextView selected = (TextView) findViewById(R.id.selected);

		FAVORITE.add(selected.getText().toString());
		Toast.makeText(this, "즐겨찾기에 추가 되었습니다", Toast.LENGTH_LONG).show();

		setFavorite();
		getFavorite();
	}

	public void deleteFavorite() {
		TextView selected = (TextView) findViewById(R.id.selected);

		FAVORITE.remove(selected.getText().toString());
		Toast.makeText(this, "즐겨찾기에서 삭제되었습니다", Toast.LENGTH_LONG).show();

		setFavorite();
		getFavorite();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		final Toast finishToast = Toast.makeText(this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다", Toast.LENGTH_LONG);
		
		SlidingDrawer drawer = (SlidingDrawer) findViewById(R.id.slidingdrawer);
		SlidingDrawer drawerR = (SlidingDrawer) findViewById(R.id.slidingdrawerR);
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (drawerR.isOpened()) {
				drawerR.animateClose();
			} else if (drawer.isOpened()) {
				drawer.animateClose();
			} else if (!isFinish){
				finishToast.show();
				isFinish = true;
				timer.schedule(new TimerTask() {
					@Override
				    public void run() {
						finishToast.cancel();
						isFinish = false;
				    }
				}, 2000);
			} else {
				finishToast.cancel();
                finish();
			}
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		myLocationOverlay.enableCompass();
		myLocationOverlay.enableMyLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();

		myLocationOverlay.disableCompass();
		myLocationOverlay.getOrientation();
		myLocationOverlay.disableMyLocation();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}