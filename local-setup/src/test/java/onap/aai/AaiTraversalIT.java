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

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.KEBAB_CASE;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static onap.aai.util.AaiRequest.v14;
import static onap.aai.util.Resources.inputStreamFrom;
import static onap.aai.util.Resources.rawTextFrom;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import java.io.UncheckedIOException;
import onap.aai.dto.Model;
import onap.aai.dto.ModelGenerator;
import onap.aai.util.AaiRequest;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class AaiTraversalIT {

    private static final String APPLICATION_XML = "application/xml";

    private final ObjectMapper objectMapper = new ObjectMapper().setPropertyNamingStrategy(KEBAB_CASE);

    @Before
    public void prepare_aai_resources() throws Exception {
        Model model = new Model("action", "123", "456");
        assertThat(objectMapper.writeValueAsString(model))
            .isEqualToIgnoringWhitespace(rawTextFrom("example_model.json"));

        System.out.println("Adding models...");
        assert_putModels_succeed();

        System.out.println("Adding query...");
        assert_putQuery_returns_created();

        System.out.println("Adding customer...");
        assert_putCustomer_returns_created();

        System.out.println("Adding VNF...");
        assert_putVnf_returns_created();
    }

    @Test
    public void aai_traversal_docker_test() throws Exception {
        assert_postQuery_returns_inventory();
    }

    private void assert_putModels_succeed() {
        ModelGenerator
            .generate("models.csv")
            .forEach(this::assert_putModel_returns_created);
    }

    private void assert_putModel_returns_created(Model model) {
        HttpRequest request;
        try {
            request = AaiRequest
                .put(v14("/service-design-and-creation/models/model/" + model.getModelInvariantId()))
                .send(objectMapper.writeValueAsString(model));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

        assertThat(request.code()).isEqualTo(HTTP_CREATED);
    }

    private void assert_putQuery_returns_created() {
        HttpRequest request = AaiRequest
            .put(v14("/service-design-and-creation/named-queries/named-query/0367193e-c785-4d5f-9cb8-7bc89dc9ddb7"))
            .send(inputStreamFrom("create_query.json"));

        assertThat(request.code()).isEqualTo(HTTP_CREATED);
    }

    private void assert_putCustomer_returns_created() {
        HttpRequest request = AaiRequest
            .put(v14("/business/customers/customer/aai_demo_for_onap_community"))
            .contentType(APPLICATION_XML)
            .send(inputStreamFrom("add_customer.xml"));

        assertThat(request.code()).isEqualTo(HTTP_CREATED);
    }

    private void assert_putVnf_returns_created() {
        HttpRequest request = AaiRequest
            .put(v14("/network/generic-vnfs/generic-vnf/de7cc3ab-0212-47df-9e64-da1c79234deb"))
            .contentType(APPLICATION_XML)
            .send(inputStreamFrom("add_generic_vnf.xml"));

        assertThat(request.code()).isEqualTo(HTTP_CREATED);
    }

    private void assert_postQuery_returns_inventory() throws JSONException {
        HttpRequest request = AaiRequest
            .post("/search/named-query")
            .contentType(HttpRequest.CONTENT_TYPE_JSON)
            .send(inputStreamFrom("execute_query.json"));

        assertThat(request.code()).isEqualTo(HTTP_OK);
        JSONAssert.assertEquals(rawTextFrom("query_response.json"), request.body(), JSONCompareMode.LENIENT);
    }
}