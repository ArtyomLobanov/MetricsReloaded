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

import weka.core.Attribute;
import weka.core.Instance;

import java.io.Serializable;

class Statement implements Serializable {
    private final String attributeName;
    private final Condition condition;
    private final double splitValue;

    Statement(String attributeName, Condition condition, double splitValue) {
        this.attributeName = attributeName;
        this.condition = condition;
        this.splitValue = splitValue;
    }

    boolean match(Instance instance) {
        final Attribute attribute = instance.dataset().attribute(attributeName);
        if (attribute == null) {
            return false;
        }
        final double value = instance.value(attribute.index());
        return value != Instance.missingValue() && ((condition == Condition.MORE) ^ (value < splitValue));
    }

    enum Condition {MORE, LESS}
}
