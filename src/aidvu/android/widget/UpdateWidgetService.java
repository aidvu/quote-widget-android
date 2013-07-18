package aidvu.android.widget;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service
{

	private static final String LOG = "aidvu.android.widget.QuoteWidget";
	
	private static final String url = "http://79.143.179.5:1337/"; 
	private static int order = new Random().nextInt(4);
	
	private static JSONArray quotes;
	private static String date = "0000-00-00";
			
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(LOG, "onStartCommand method called");
		
		super.onStartCommand(intent, flags, startId);
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
		RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.main);

		int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		// Register an onClickListener
		Intent clickIntent = new Intent(this.getApplicationContext(), QuoteWidget.class);

		clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_textview_quote, pendingIntent);
		remoteViews.setOnClickPendingIntent(R.id.widget_textview_author, pendingIntent);
		
		for (int widgetId : allWidgetIds)
		{
			try
			{
				if (quotes == null && date != getDate()) {
					// Create a new HTTP Client
					DefaultHttpClient defaultClient = new DefaultHttpClient();
					// Setup the get request
					HttpGet httpGetRequest = new HttpGet(url);

					// Execute the request in the client
					HttpResponse httpResponse = defaultClient.execute(httpGetRequest);
					// Grab the response
					BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
					String json = "";
					String line;
					while ((line = reader.readLine()) != null)
					{
						json += line;
					}

					// Instantiate a JSON object from the request response
					quotes = new JSONArray(json);
					date = getDate();
				}
				
				JSONObject jsonObject = quotes.getJSONObject(order);
				
				// Pick the quote without the quotes
				remoteViews.setTextViewText(R.id.widget_textview_quote, jsonObject.getString("quote").substring(1, jsonObject.getString("quote").length() - 1));
				// Pick the author
				remoteViews.setTextViewText(R.id.widget_textview_author, jsonObject.getString("author"));

				order = (order + 1) % 4;

				// Update the widget
				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
			catch(Exception e)
			{
				Log.e(LOG, "Error fetching quotes");
				e.printStackTrace();
				
				// Display an error message
				remoteViews.setTextViewText(R.id.widget_textview_quote, getString(R.string.error_fetch_quotes));
				// Blank the author
				remoteViews.setTextViewText(R.id.widget_textview_author, getString(R.string.empty_string));
			}
		}

		stopSelf();

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	/**
	 * Returns current date
	 * 
	 * @return {@link String} Current date in format 'yyyy-mm-dd'
	 */
	private String getDate() {
		Calendar c = Calendar.getInstance();

		return c.get(Calendar.YEAR) + "-"
				+ c.get(Calendar.MONTH) + "-"
				+ c.get(Calendar.DAY_OF_MONTH);
	}
}
