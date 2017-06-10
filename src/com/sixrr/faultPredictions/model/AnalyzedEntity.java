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

package com.sixrr.faultPredictions.model;

public class AnalyzedEntity {
    private final String name;
    private final String comment;
    private final double isDefective;

    public AnalyzedEntity(String name, String comment, double isDefective) {
        this.name = name;
        this.comment = comment;
        this.isDefective = isDefective;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public double isDefective() {
        return isDefective;
    }
}
