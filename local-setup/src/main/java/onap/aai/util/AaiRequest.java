/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2018-2019 Nokia Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package onap.aai.util;

import com.github.kevinsawicki.http.HttpRequest;

public class AaiRequest {

    private static final String AAI_BASE_URL = "https://localhost:8443/aai";
    private static final String AAI_AUTH = "Basic QUFJOkFBSQ==";
    private static final String SCHEMA_VERSION = "/v14";

    public static HttpRequest get(String endpoint) {
        return aaiRequest(HttpRequest.get(aaiEndpoint(endpoint)));
    }

    public static HttpRequest post(String endpoint) {
        return aaiRequest(HttpRequest.post(aaiEndpoint(endpoint)));
    }

    public static HttpRequest put(String endpoint) {
        return aaiRequest(HttpRequest.put(aaiEndpoint(endpoint)));
    }

    public static HttpRequest delete(String endpoint) {
        return aaiRequest(HttpRequest.delete(aaiEndpoint(endpoint)));
    }

    public static String v14(String endpoint) {
        return SCHEMA_VERSION + endpoint;
    }

    private static HttpRequest aaiRequest(HttpRequest httpRequest) {
        return httpRequest
            .header("X-FromAppId", "dummy id")
            .header("X-TransactionId", "1234")
            .authorization(AAI_AUTH)
            .trustAllCerts()
            .trustAllHosts();
    }

    private static String aaiEndpoint(String endpoint) {
        return AAI_BASE_URL + endpoint;
    }
}