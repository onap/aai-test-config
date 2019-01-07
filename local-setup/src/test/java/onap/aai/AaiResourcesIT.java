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
package onap.aai;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static onap.aai.util.AaiRequest.v14;
import static onap.aai.util.Resources.inputStreamFrom;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.kevinsawicki.http.HttpRequest;
import com.jayway.jsonpath.JsonPath;
import onap.aai.util.AaiRequest;
import org.junit.After;
import org.junit.Test;

public class AaiResourcesIT {

    private String id;

    @After
    public void tearDown() {
        if (id != null && !id.isEmpty()) {
            AaiRequest.delete(v14("/cloud-infrastructure/complexes/complex/clli2?resource-version=" + id));
        }
    }

    @Test
    public void aai_resources_docker_test() {
        System.out.println("Get complexes...");
        assert_getComplexes_returns_notFound();

        System.out.println("Adding complex...");
        assert_putComplex_returns_created();

        System.out.println("Get complexes...");
        id = assert_getComplexes_returns_complex();

        System.out.println("Remove complex...");
        assert_deleteComplex_returns_noContent(id);

        System.out.println("Get complex...");
        assert_getComplexes_returns_notFound();
    }

    private void assert_getComplexes_returns_notFound() {
        HttpRequest request = AaiRequest.get(v14("/cloud-infrastructure/complexes"));

        assertThat(request.code()).isEqualTo(HTTP_NOT_FOUND);
        assertThat(request.body()).contains("requestError");
    }

    private void assert_putComplex_returns_created() {
        HttpRequest request = AaiRequest
            .put(v14("/cloud-infrastructure/complexes/complex/clli2"))
            .send(inputStreamFrom("complex_data.json"));

        assertThat(request.code()).isEqualTo(HTTP_CREATED);
    }

    private String assert_getComplexes_returns_complex() {
        HttpRequest request = AaiRequest.get(v14("/cloud-infrastructure/complexes"))
            .acceptJson();

        assertThat(request.code()).isEqualTo(HTTP_OK);
        String body = request.body();

        assertThat(body).contains("clli2").contains("resource-version");

        return JsonPath.read(body, "$.complex.[0].resource-version");
    }

    private void assert_deleteComplex_returns_noContent(String id) {
        HttpRequest request = AaiRequest
            .delete(v14("/cloud-infrastructure/complexes/complex/clli2?resource-version=" + id));

        assertThat(request.code()).isEqualTo(HTTP_NO_CONTENT);
    }
}