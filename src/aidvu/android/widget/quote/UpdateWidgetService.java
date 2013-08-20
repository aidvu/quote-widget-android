package aidvu.android.widget.quote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service
{

	private static final String url = "http://codeden.net:1337/"; 

	private static final String fileName = "quote_cache";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(QuoteWidget.LOG, "onStartCommand method called");
		
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
		
		for (int widgetId : allWidgetIds) {
			try {
				Quotes quotes = loadQuotes();

				// Check if we should update the quotes
				if (quotes == null || !quotes.getDate().equals(getCurrentDate())) {
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
					while ((line = reader.readLine()) != null) {
						json += line;
					}

					JSONArray quotesJsonArray = new JSONArray(json);
					String currentDate = getCurrentDate();
					
					if (quotes == null) {
						quotes = new Quotes();	
					}
					
					quotes.setDate(currentDate);
					quotes.setQuotes(quotesJsonArray);
				}
				
				Quote quote = quotes.getNextQuote();

				saveQuotes(quotes);

				// Pick the quote without the quotes
				remoteViews.setTextViewText(R.id.widget_textview_quote, quote.getQuote());
				// Pick the author
				remoteViews.setTextViewText(R.id.widget_textview_author, quote.getAuthor());

				// Update the widget
				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			} catch(Exception e) {
				Log.e(QuoteWidget.LOG, "Error fetching quotes");
				e.printStackTrace();
				
				remoteViews.setTextViewText(R.id.widget_textview_quote, getString(R.string.error_fetch_quotes));
				remoteViews.setTextViewText(R.id.widget_textview_author, getString(R.string.author));
			}
		}
		
		stopSelf();

		return START_STICKY;
	}
	
	/**
	 * Deserializes Quotes class from file and returns it
	 * 
	 * @return {@link Quotes} Cached quotes
	 */
	private Quotes loadQuotes() {
		try {
			File file = this.getFileStreamPath(fileName);
			if (file.exists()) {
				FileInputStream fis = this.openFileInput(fileName);
				ObjectInputStream is = new ObjectInputStream(fis);
				Quotes quotes = (Quotes) is.readObject();
				is.close();
				
				return quotes;
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.e(QuoteWidget.LOG, "Error loading quotes from cache");
			e.printStackTrace();

			return null;
		}
	}
	
	/**
	 * Serializes Quotes class and caches it in a file
	 * 
	 * @param quotes {@link Quotes} to be saved to a file
	 */
	private void saveQuotes(Quotes quotes) {
		try {
			FileOutputStream fos = this.openFileOutput(fileName, Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(quotes);
			os.close();
		} catch (Exception e) {
			Log.e(QuoteWidget.LOG, "Error saving quotes to cache");
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns current date
	 * 
	 * @return {@link String} Current date in format 'yyyy-mm-dd'
	 */
	private String getCurrentDate() {
		Calendar c = Calendar.getInstance();

		return c.get(Calendar.YEAR) + "-"
				+ c.get(Calendar.MONTH) + "-"
				+ c.get(Calendar.DAY_OF_MONTH);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
