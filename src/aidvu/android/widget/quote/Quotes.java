package aidvu.android.widget.quote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Quotes implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -547240714447732256L;
	
	private String date;
	private List<Quote> quotes;
	private int quotesNumber = 4;
	private int order = new Random().nextInt(quotesNumber);
	
	public Quotes() {}
	
	public Quotes(String date, JSONArray quotes) throws JSONException {
		this.date = date;
		this.setQuotes(quotes);
	}
	
	public void setQuotes(JSONArray quotes) throws JSONException {
		this.quotes = new ArrayList<Quote>();
		
		for (int i = 0; i < quotes.length(); i++) {
			JSONObject jsonObject = quotes.getJSONObject(i);
			
			this.quotes.add(
				new Quote(
					jsonObject.getString("author"),
					jsonObject.getString("quote").substring(1, jsonObject.getString("quote").length() - 1)
				)
			);
		}
	}
	
	/**
	 * Returns the next quote for
	 * 
	 * @return {@link Quote} returns the next quote
	 */
	public Quote getNextQuote() {
		Quote quote = quotes.get(order);
		order = (order + 1) % quotesNumber;
		return quote;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
