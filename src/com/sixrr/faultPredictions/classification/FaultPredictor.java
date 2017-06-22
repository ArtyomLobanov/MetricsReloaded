/*
 * Copyright 2005-2017 Sixth and Red River Software, Bas Leijdekkers
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.faultPredictions.classification;

import com.sixrr.faultPredictions.model.AnalyzedEntity;
import com.sixrr.metrics.metricModel.MetricsResult;
import org.jetbrains.annotations.NotNull;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class FaultPredictor implements Serializable {
    private static final String PATH_TO_CLASSIFIER = "/predictor.object";
    private static final String DEFAULT_COMMENT = "no comment";
    private static FaultPredictor defaultPredictor;
    private final Classifier classifier;
    private final Map<Statement[], String> rules;

    FaultPredictor(@NotNull Classifier classifier) throws IOException, ClassNotFoundException {
        this.classifier = classifier;
        rules = new HashMap<>();
    }

    void addRule(String message, Statement... statements) {
        rules.put(statements.clone(), message);
    }

    @NotNull
    public static FaultPredictor load(String path) throws PredictorLoadingException, IOException {
        try (FileInputStream input = new FileInputStream(path);) {
            return readPredictor(input);
        }
    }

    public void save(String path) throws IOException {
        try (OutputStream fileOutputStream = new FileOutputStream(path);
             ObjectOutput output = new ObjectOutputStream(fileOutputStream)) {
            output.writeObject(this);
        }
    }

    @NotNull
    public static FaultPredictor loadDefaultPredictor() throws PredictorLoadingException, IOException {
        if (defaultPredictor != null) {
            return defaultPredictor;
        }
        try (InputStream input = FaultPredictor.class.getResourceAsStream(PATH_TO_CLASSIFIER)) {
            if (input == null) {
                throw new PredictorLoadingException("Resource file wasn't found: " + PATH_TO_CLASSIFIER);
            }
            defaultPredictor = readPredictor(input);
        }
        return defaultPredictor;
    }

    @NotNull
    private static FaultPredictor readPredictor(InputStream input) throws IOException, PredictorLoadingException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(input)) {
            return (FaultPredictor) objectInputStream.readObject();
        } catch (ObjectStreamException | ClassNotFoundException e) {
            throw new PredictorLoadingException("Can't read classifier because it has unexpected format", e);
        }
    }

    private static boolean isRuleMatched(Statement[] statements, Instance instance) {
        return Arrays.stream(statements)
                .allMatch(s -> s.match(instance));
    }

    private String getMessage(Instance instance) {
        final String comments = rules.entrySet().stream()
                .filter(e -> isRuleMatched(e.getKey(), instance))
                .map(Entry::getValue)
                .collect(Collectors.joining("; "));
        return comments.isEmpty()? DEFAULT_COMMENT : comments;
    }

    public AnalyzedEntity[] analyze(MetricsResult metricsResult) throws ClassificationException {
        final Instances instances = InstancesCreator.createInstances(metricsResult);
        final String[] entities = metricsResult.getMeasuredObjects();
        final AnalyzedEntity[] results = new AnalyzedEntity[entities.length];
        try {
            for (int i = 0; i < entities.length; i++) {
                final Instance instance = instances.instance(i);
                final double isDefective = classifier.distributionForInstance(instance)[1];
                results[i] = new AnalyzedEntity(entities[i], getMessage(instance), isDefective);
            }
        } catch (Exception e) {
            throw new ClassificationException("Error occurred during classification: " + e.getMessage(), e);
        }
        return results;
    }

    public static final class PredictorLoadingException extends Exception {
        private PredictorLoadingException(String message) {
            super(message);
        }

        private PredictorLoadingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static final class ClassificationException extends Exception {
        public ClassificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
