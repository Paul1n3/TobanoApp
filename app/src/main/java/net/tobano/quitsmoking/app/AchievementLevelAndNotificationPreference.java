package net.tobano.quitsmoking.app;

import java.io.Serializable;
import java.util.Date;

public class AchievementLevelAndNotificationPreference implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Date date;
	private String title;
	private String text;
	private String category;
	private String level;
	private int id;

	public AchievementLevelAndNotificationPreference(Date date, String title,
			String text, String category, String level, int id) {
		super();
		this.date = date;
		this.title = title;
		this.text = text;
		this.category = category;
		this.level = level;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}
