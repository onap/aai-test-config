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

import static onap.aai.util.Resources.readerFrom;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ModelGenerator {

    public static Stream<Model> generate(String fileName) {
        try (CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(readerFrom(fileName))) {
            return parser.getRecords().stream().map(ModelGenerator::csvToModel);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Model csvToModel(CSVRecord csvRecord) {
        return new Model(
            csvRecord.get("model-name"),
            csvRecord.get("model-invariant-id"),
            csvRecord.get("model-version-id")
        );
    }
}
