package jp.mokejp.gilw;

public class GoogleImageResult {
	private String m_title;
	private String m_url;
	
	public GoogleImageResult(String title, String url) {
		m_title = title;
		m_url = url;
	}
	
	public String getTitle() {
		return m_title;
	}
	
	public String getUrl() {
		return m_url;
	}
}
