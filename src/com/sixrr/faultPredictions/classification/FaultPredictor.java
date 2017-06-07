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

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class FaultPredictor {
    private static final String PATH_TO_CLASSIFIER = "/classifier.object";
    private final Classifier classifier;

    public FaultPredictor() throws IOException, ClassNotFoundException {
        try (InputStream resourceStream = FaultPredictor.class.getResourceAsStream(PATH_TO_CLASSIFIER);
             ObjectInputStream input = new ObjectInputStream(resourceStream)) {
            classifier = (Classifier) input.readObject();
        }
    }

    public boolean isDefective(Instance instance) {
        try {
            return classifier.distributionForInstance(instance)[1] > 0.5;
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong", e);
        }
    }
}
