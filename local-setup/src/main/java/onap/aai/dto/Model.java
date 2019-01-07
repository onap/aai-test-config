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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package onap.aai.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Dummy model for testing purposes
 */
@SuppressWarnings("unused")
public class Model {

    private static final String MODEL_TYPE = "widget";
    private static final String MODEL_VERSION = "1.0";

    private final String modelInvariantId;
    private final String modelType;
    private final ModelVers modelVers;

    public Model(String modelName, String modelInvariantId, String modelVersionId) {
        this.modelInvariantId = modelInvariantId;
        this.modelVers = new ModelVers(new ModelVer(modelVersionId, modelName));
        this.modelType = MODEL_TYPE;
    }

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public ModelVers getModelVers() {
        return modelVers;
    }

    public String getModelType() {
        return modelType;
    }

    private static class ModelVers {

        private final List<ModelVer> modelVer;

        ModelVers(ModelVer... modelVer) {
            this.modelVer = Arrays.asList(modelVer);
        }

        public List<ModelVer> getModelVer() {
            return modelVer;
        }
    }

    private static class ModelVer {

        private final String modelVersionId;
        private final String modelVersion;
        private final String modelName;

        ModelVer(String modelVersionId, String modelName) {
            this.modelVersionId = modelVersionId;
            this.modelName = modelName;
            this.modelVersion = MODEL_VERSION;
        }

        public String getModelVersionId() {
            return modelVersionId;
        }

        public String getModelName() {
            return modelName;
        }

        public String getModelVersion() {
            return MODEL_VERSION;
        }
    }
}