package com.example.searchingapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.searchingapp.database.Database;


/**
 * The Class MainScreen.
 */
public class MainScreen extends FragmentActivity implements
		LoaderCallbacks<Cursor> {

	/** The Constant TAG_VALUE. */
	private static final String TAG_VALUE = "value";

	/** The Constant TAG_RESULT. */
	private static final String TAG_RESULT = "RESULT";

	/** The Constant NEGATIVE_BUTTON_MESSAGE. */
	private static final String NEGATIVE_BUTTON_MESSAGE =
			"Действие отменено";

	/** The Constant POSITIVE_BUTTON_MESSAGE. */
	private static final String POSITIVE_BUTTON_MESSAGE = 
			"Данные удалены";

	/** The Constant first part of SEARCH_URL. */
	private static final String SEARCH_URL = "https://www.googleapis.com/"
			+ "customsearch/v1?key=AIzaSyBP5MRjJKaSwnJTDzbxEyL8QvakY0R_"
			+ "dns&cx=005082323668398804698:ggtpl2jerrm&q=";

	/** The Constant second part of SEARCH_URL_END. */
	private static final String SEARCH_URL_END = "&alt=json";

	/** The search value. */
	String searchItem;

	/** The search query. */
	String searchQuery;

	/** The database. */
	Database database;

	/** The context. */
	Context context;

	/** The current search ListView. */
	ListView currentSearchLV;

	/** The search history ListView. */
	ListView searchHistoryLV;

	/** The search EditText field. */
	EditText searchValueET;

	/** Adapter for search history. */
	SimpleCursorAdapter historyAdapter;

	/** The cur srch title result. */
	TextView curSrchTitleValue, curSrchTitleResult;

	/** The current search list. */
	ArrayList<HashMap<String, String>> currentSearchList;

	/** The current search adapter. */
	ListAdapter currentSearchAdapter;

	/**
	 * Creates Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);

		//Connect to database.
		database = new Database(this);
		//Open database.
		database.open();
		
		currentSearchList = new ArrayList<HashMap<String, String>>();
		
		String[] fromHistory = { Database.COLUMN_VALUE,
				Database.COLUMN_RESULT };
		int[] toHistory = { R.id.item_value,
				R.id.curr_search_result_tv };

		historyAdapter = new SimpleCursorAdapter(this,
				R.layout.search_list_item, null, fromHistory, toHistory, 0);
		searchHistoryLV = (ListView) findViewById(R.id.search_history_list);
		searchHistoryLV.setAdapter(historyAdapter);
		searchHistoryLV.requestFocus();

		currentSearchLV = (ListView) findViewById(R.id.current_search_list);
		searchValueET = (EditText) findViewById(R.id.searchingValueEt);
		curSrchTitleValue = (TextView) findViewById(R.id.curr_search_value_tv);
		curSrchTitleResult = (TextView) findViewById(R.id.curr_search_result_tv);

		String[] fromCurrSearch = new String[] { TAG_VALUE, TAG_RESULT };
		int[] toCurrSearch = new int[] { R.id.item_value,
				R.id.curr_search_result_tv };
		currentSearchAdapter = new SimpleAdapter(this, currentSearchList,
				R.layout.search_list_item1, fromCurrSearch, toCurrSearch);

		if (currentSearchList.isEmpty()) {
			curSrchTitleValue.setVisibility(View.GONE);
			curSrchTitleResult.setVisibility(View.GONE);
		} else {
			currentSearchLV.setAdapter(currentSearchAdapter);
		}

		//creates loader
		getSupportLoaderManager().initLoader(0, null, this);

	}

	/**
	 * Saves current search list when orientation changes
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (currentSearchList != null) {
			for (int i = 0; i < currentSearchList.size(); i++) {
				JSONObject json = new JSONObject();
				try {
					json.put("map" + Integer.toString(i),
							currentSearchList.get(i));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				outState.putString("list" + Integer.toString(i),
						json.toString());
			}
			outState.putInt("list", currentSearchList.size());
		}
		currentSearchLV.setAdapter(currentSearchAdapter);
	}

	/**
	 * Loads saved search on when orientation changes.
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int k = savedInstanceState.getInt("list");
			for (int i = 0; i < k; i++) {
				String[] temp = savedInstanceState.getString(
						"list" + Integer.toString(i)).split("value=");
				String[] tempTrimmed = temp[1].split(", RESULT=");
				String value = tempTrimmed[0];
				String result = tempTrimmed[1].substring(0,
						tempTrimmed[1].length() - 3);
				HashMap<String, String> search = new HashMap<String, String>();
				search.put(TAG_VALUE, value);
				search.put(TAG_RESULT, result);
				currentSearchList.add(search);
			}
			if (!currentSearchList.isEmpty()) {
			currentSearchLV.setAdapter(currentSearchAdapter);
			curSrchTitleValue.setVisibility(View.VISIBLE);
			curSrchTitleResult.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * Search button click listener.
	 * 
	 * @param v view
	 */
	public void searchButtonPressed(View v) {
		searchItem = searchValueET.getText().toString();
		if(!searchItem.equals("")){
		try {
			searchItem = URLEncoder.encode(searchItem, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		searchQuery = SEARCH_URL + searchItem + SEARCH_URL_END;

		new JsonSearchTask().execute();
		} else {
			Toast.makeText(this, "Введите значение поиска", Toast.LENGTH_SHORT)
			.show();
		}
	}

	/**
	 * Disconects from database and destroy loader when activity destroyed.
	 */
	@Override
	protected void onDestroy() {
		getSupportLoaderManager().destroyLoader(0);
		database.close();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * Do negative click in deleting data dialog.
	 */
	void doNegativeClick() {
		Toast.makeText(this, NEGATIVE_BUTTON_MESSAGE, Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * Do positive click on deleting data dialog.
	 * Deletes all search history.
	 */
	void doPositiveClick() {
		database.deleteAll();
		getSupportLoaderManager().getLoader(0).forceLoad();
		Toast.makeText(this, POSITIVE_BUTTON_MESSAGE, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.deleteAll:
			new AlertDialog.Builder(this)
					.setTitle(R.string.delete_dialog_title)
					.setPositiveButton(R.string.yes_string,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog,
										int which) {
									doPositiveClick();
								}
							})
					.setNegativeButton(R.string.no_string,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog,
										int which) {
									doNegativeClick();
								}
							}).show();
			return true;
		case R.id.exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Creates new loader.
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new MyCursorLoader(this, database);
	}

	/**
	 * Fills search history from database.
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> loader,
			Cursor cursor) {
		historyAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	/**
	 * Send query.
	 * 
	 * @param query search
	 * @return JSON string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String sendQuery(String query) throws IOException {
		String result = "";

		URL searchURL = new URL(query);

		HttpURLConnection httpURLConnection = (HttpURLConnection) searchURL
				.openConnection();

		if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			InputStreamReader inputStreamReader = new InputStreamReader(
					httpURLConnection.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader, 8192);

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				result += line;
			}

			bufferedReader.close();
		}

		return result;
	}

	/**
	 * Parses json string  to search results.
	 * 
	 * @param json string
	 * @throws JSONException
	 *             the JSON exception
	 */
	private void ParseResult(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		JSONArray jsonArray_results = jsonObject.getJSONArray("items");
		String tempResult;
		String tempValue = searchValueET.getText().toString();

		for (int i = 0; i < jsonArray_results.length(); i++) {
			JSONObject jsonObject_i = jsonArray_results.getJSONObject(i);
			tempResult = jsonObject_i.getString("snippet").substring(0, 14)
					.toString();
			HashMap<String, String> search = new HashMap<String, String>();
			search.put(TAG_VALUE, tempValue);
			search.put(TAG_RESULT, tempResult);
			database.addRec(tempValue, tempResult);
			currentSearchList.add(search);

		}

	}

	/**
	 * The Class JsonSearchTask.
	 */
	private class JsonSearchTask extends AsyncTask<Void, Void, Void> {

		/**
		 * Parses JSON string in background thread
		 */
		@Override
		protected Void doInBackground(Void... arg0) {

			try {
				ParseResult(sendQuery(searchQuery));
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * Fills data from parsed JSON string to ListView
		 */
		@Override
		protected void onPostExecute(Void result) {

			currentSearchLV.setAdapter(currentSearchAdapter);
			curSrchTitleValue.setVisibility(View.VISIBLE);
			curSrchTitleResult.setVisibility(View.VISIBLE);
			getSupportLoaderManager().getLoader(0).forceLoad();
			super.onPostExecute(result);
		}
	}

	/**
	 * The Class MyCursorLoader.
	 */
	static class MyCursorLoader extends CursorLoader {

		/**
		 * Instantiates a new my cursor loader.
		 * 
		 * @param context
		 *            the context
		 * @param database
		 *            the database
		 */
		public MyCursorLoader(Context context, Database database) {
			super(context);
			this.database = database;
		}

		/** The database. */
		Database database;

		/**
		 * Loads cursor in new thread
		 */
		@Override
		public Cursor loadInBackground() {
			Cursor cursor = database.getAllData();
			return cursor;

		}

	}
}
