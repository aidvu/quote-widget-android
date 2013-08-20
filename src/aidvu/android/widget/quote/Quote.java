package aidvu.android.widget.quote;

import java.io.Serializable;

public class Quote implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6991712671505878185L;

	public Quote(String author, String quote) {
		this.author = author;
		this.quote = quote;
	}

	private String author;
	private String quote;
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getQuote() {
		return quote;
	}
	
	public void setQuote(String quote) {
		this.quote = quote;
	}
}
