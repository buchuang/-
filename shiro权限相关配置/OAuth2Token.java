package cn.proflu.profluweb.common.oauth2;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * token
 *
 * @date 2017-05-20 13:22
 */
public class OAuth2Token implements AuthenticationToken {
	private String token;

	public OAuth2Token(String token) {
		this.token = token;
	}

	@Override
	public String getPrincipal() {
		return token;
	}

	@Override
	public Object getCredentials() {
		return token;
	}
}
