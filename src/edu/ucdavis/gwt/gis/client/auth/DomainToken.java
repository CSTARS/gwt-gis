package edu.ucdavis.gwt.gis.client.auth;

public class DomainToken {

	private String domain = "";
	private String token = "";
	private String username = "";
	
	public DomainToken() {}
	
	public DomainToken(String domain, String token, String username) {
		setDomain(domain);
		this.token = cleanToken(token);
		this.username = username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	public String getUsername() {
		return username;
	}
	
	public void setToken(String token) {
		this.token = cleanToken(token);
	}
	public String getToken() {
		return token;
	}

	public void setDomain(String domain) {
		this.domain = cleanDomain(domain);
	}

	public String getDomain() {
		return domain;
	}
	
	private String cleanDomain(String domain) {
		String[] parts = domain.split(":\\/\\/");
		if( parts.length > 1 ) {
			return parts[1].replaceAll("\\/.*", "").replaceAll("\\s*", "");
		} 
		return domain.replaceAll("\\/.*", "").replaceAll("\\s*", "");
	}
	
	private String cleanToken(String token) {
		return token.replaceAll("\\s*", "").replaceAll("\\t", "");
	}


}
