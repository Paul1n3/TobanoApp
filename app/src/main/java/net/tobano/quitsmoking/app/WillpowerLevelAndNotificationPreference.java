package net.tobano.quitsmoking.app;

import java.io.Serializable;
import java.util.Date;

public class WillpowerLevelAndNotificationPreference implements Serializable {

	private static final long serialVersionUID = 1L;
	private Date date;
	private String title;
	private String text;
	private String level;
	private int id;

	public WillpowerLevelAndNotificationPreference(Date date, String title,
			String text, String level, int id) {
		super();
		this.date = date;
		this.title = title;
		this.text = text;
		this.level = level;
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

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
