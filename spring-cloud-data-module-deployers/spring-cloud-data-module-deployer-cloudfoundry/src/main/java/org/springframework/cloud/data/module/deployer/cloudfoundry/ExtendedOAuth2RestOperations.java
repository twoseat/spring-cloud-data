package org.springframework.cloud.data.module.deployer.cloudfoundry;

import java.net.URI;

import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.web.client.RestClientException;

/**
 * An extension to RestOperations, analogous to postForObject().
 *
 * @author Eric Bottard
 */
public interface ExtendedOAuth2RestOperations extends OAuth2RestOperations {

	<T> T putForObject(URI uri, Object request, Class<T> responseType) throws RestClientException;

}
