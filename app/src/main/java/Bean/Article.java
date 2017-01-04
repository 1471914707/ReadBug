package Bean;

public class Article {
	Integer id;
	String name;
	String title;
	String time;
	String type;
	String content;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {

		return content;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {

		return type;
	}

	int pageView;

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setPageView(int pageView) {
		this.pageView = pageView;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getTime() {
		return time;
	}

	public int getPageView() {
		return pageView;
	}
}
