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

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;

import java.io.*;

public class FaultPredictor {
    private static final String PATH_TO_CLASSIFIER = "/classifier.object";
    private final Classifier classifier;

    private FaultPredictor(@NotNull Classifier classifier) throws IOException, ClassNotFoundException {
        this.classifier = classifier;
    }

    @Nullable
    public static FaultPredictor load(String path) throws PredictorLoadingException, IOException {
        try (FileInputStream input = new FileInputStream(path);) {
            return readPredictor(input);
        }
    }

    @Nullable
    public static FaultPredictor loadDefaultPredictor() throws PredictorLoadingException, IOException {
        try (InputStream input = FaultPredictor.class.getResourceAsStream(PATH_TO_CLASSIFIER)){
            if (input == null) {
                throw new PredictorLoadingException("Resource file wasn't found: " + PATH_TO_CLASSIFIER);
            }
            return readPredictor(input);
        }
    }

    @Nullable
    private static FaultPredictor readPredictor(InputStream input) throws IOException, PredictorLoadingException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(input)) {
            return new FaultPredictor((Classifier) objectInputStream.readObject());
        } catch (ObjectStreamException | ClassNotFoundException e) {
            throw new PredictorLoadingException("Can't read classifier because it has unexpected format", e);
        }
    }

    public boolean isDefective(Instance instance) {
        try {
            return classifier.distributionForInstance(instance)[1] > 0.5;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong", e);
        }
    }

    public static class PredictorLoadingException extends Exception {
        private PredictorLoadingException(String message) {
            super(message);
        }

        private PredictorLoadingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
