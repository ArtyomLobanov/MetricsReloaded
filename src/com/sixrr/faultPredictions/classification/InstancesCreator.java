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

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.stockmetrics.methodMetrics.*;
import org.jetbrains.annotations.Nullable;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class InstancesCreator {

    private static final double HALSTEAD_ERROR_SCALE = 1.0 / 3000;
    private static final double HALSTEAD_TIME_SCALE = 1.0 / 18;
    private static final Attribute CLASS_ATTRIBUTE;
    private static final Map<String, AttributeConverters> converters = new HashMap<>();

    static {
        final FastVector classValues = new FastVector(2);
        classValues.addElement("false");
        classValues.addElement("true");
        CLASS_ATTRIBUTE = new Attribute("defects", classValues);
        converters.put("branch_count", new DifferenceConverter(BranchCountMetric.class));
        converters.put("condition_count", new DifferenceConverter(ConditionCountMetric.class));
        converters.put("cyclomatic_complexity", new DifferenceConverter(CyclomaticComplexityMetric.class));
        converters.put("cyclomatic_density", new QuotientConverter(CyclomaticComplexityMetric.class,
                LinesOfCodeMethodMetric.class));
        converters.put("decision_count", new DifferenceConverter(DecisionCountMetric.class));
        converters.put("design_complexity", new DifferenceConverter(DesignComplexityMetric.class));
        converters.put("design_density", new DifferenceConverter(DesignDensityMetric.class));
        converters.put("executable_loc", new DifferenceConverter(LinesOfCodeMethodMetric.class,
                BlankLinesCountMethodMetric.class, CommentLinesOfCodeMethodMetric.class));
        converters.put("formal_parameters", new DifferenceConverter(FormalParametersCountMethodMetric.class));
        converters.put("halstead_difficulty", new DifferenceConverter(HalsteadDifficultyMethodMetric.class));
        converters.put("halstead_effort", new DifferenceConverter(HalsteadEffortMethodMetric.class));
        converters.put("halstead_error", new ScaledConverter(HalsteadVolumeMethodMetric.class, HALSTEAD_ERROR_SCALE));
        converters.put("halstead_length", new DifferenceConverter(HalsteadLengthMethodMetric.class));
        converters.put("halstead_level", new DifferenceConverter(HalsteadProgramLevelMetric.class));
        converters.put("halstead_time", new ScaledConverter(HalsteadEffortMethodMetric.class, HALSTEAD_TIME_SCALE));
        converters.put("halstead_volume", new DifferenceConverter(HalsteadVolumeMethodMetric.class));
        converters.put("normalized_cyclomatic_complexity", new QuotientConverter(CyclomaticComplexityMetric.class,
                LinesOfCodeMethodMetric.class));
        converters.put("total_operands", new DifferenceConverter(OperadsCountMetric.class));
        converters.put("total_operators", new DifferenceConverter(OperatorsCountMetric.class));
        converters.put("unique_operands", new DifferenceConverter(DistinctOperandsMetric.class));
        converters.put("unique_operators", new DifferenceConverter(DistinctOperatorsMetric.class));
        converters.put("total_loc", new DifferenceConverter(LinesOfCodeMethodMetric.class));
//        converters.put("blank_loc", new DifferenceConverter(BlankLinesCountMethodMetric.class));
//        converters.put("comment_loc", new DifferenceConverter(CommentLinesOfCodeMethodMetric.class));
//        converters.put("code_and_comment_loc", new DifferenceConverter(LinesOfCodeMethodMetric.class,
//                BlankLinesCountMethodMetric.class));
//        converters.put("halstead_vocabulary", new DifferenceConverter(HalsteadVocabularyMethodMetric.class));
//        converters.put("decision_density", new QuotientConverter(DecisionCountMetric.class,
//                LinesOfCodeMethodMetric.class));
        converters.put(CLASS_ATTRIBUTE.name(), (r, m, t) -> Double.valueOf(Instance.missingValue()));
    }

    private InstancesCreator() {
    }

    public static Instances createInstances(MetricsResult metricsResult) {
        final Map<Class<? extends Metric>, Metric> metrics = Arrays.stream(metricsResult.getMetrics())
                .collect(Collectors.toMap(Metric::getClass, Function.identity()));
        final FastVector attributes = new FastVector(converters.size());
        converters.keySet().stream()
                .sequential()
                .filter(s -> !s.equals(CLASS_ATTRIBUTE.name()))
                .map(Attribute::new)
                .forEach(attributes::addElement);
        attributes.addElement(CLASS_ATTRIBUTE.copy());
        final String[] methods = metricsResult.getMeasuredObjects();
        final Instances instances = new Instances("tested methods", attributes, methods.length);
        instances.setClass(CLASS_ATTRIBUTE);
        for (String method : methods) {
            final double[] attributesValues = new double[attributes.size()];
            for (int i = 0; i < instances.numAttributes(); i++) {
                final String attribute = instances.attribute(i).name();
                final Double value = converters.get(attribute).getValue(metricsResult, metrics, method);
                attributesValues[i] = value == null ? Instance.missingValue() : value.doubleValue();
            }
            final Instance instance = new Instance(1.0, attributesValues);
            instances.add(instance);
            instance.setDataset(instances);
        }
        return instances;
    }

    @FunctionalInterface
    private interface AttributeConverters {
        @Nullable
        Double getValue(MetricsResult result, Map<Class<? extends Metric>, Metric> metrics, String target);
    }

    private static final class DifferenceConverter implements AttributeConverters {
        private final Class<? extends Metric> metric;
        private final Class<? extends Metric>[] metricsToSubtract;

        @SafeVarargs
        private DifferenceConverter(Class<? extends Metric> metric, Class<? extends Metric>... metricsToSubtract) {
            this.metric = metric;
            this.metricsToSubtract = metricsToSubtract;
        }

        @Override
        @Nullable
        public Double getValue(MetricsResult result, Map<Class<? extends Metric>, Metric> metrics, String target) {
            Double value = result.getValueForMetric(metrics.get(metric), target);
            if (value == null) {
                return null;
            }
            double res = value.doubleValue();
            for (Class<? extends Metric> metricToSubtract : metricsToSubtract) {
                value = result.getValueForMetric(metrics.get(metricToSubtract), target);
                if (value == null) {
                    return null;
                }
                res -= value.doubleValue();
            }
            return Double.valueOf(res);
        }
    }

    private static final class ScaledConverter implements AttributeConverters {
        private final Class<? extends Metric> metric;
        private final double scale;

        private ScaledConverter(Class<? extends Metric> metric, double scale) {
            this.metric = metric;
            this.scale = scale;
        }

        @Override
        @Nullable
        public Double getValue(MetricsResult result, Map<Class<? extends Metric>, Metric> metrics, String target) {
            final Double value = result.getValueForMetric(metrics.get(metric), target);
            return value == null ? null : Double.valueOf(value.doubleValue() * scale);
        }
    }

    private static final class QuotientConverter implements AttributeConverters {
        private final Class<? extends Metric> numeratorMetric;
        private final Class<? extends Metric> denominatorMetric;

        private QuotientConverter(Class<? extends Metric> numeratorMetric, Class<? extends Metric> denominatorMetric) {
            this.numeratorMetric = numeratorMetric;
            this.denominatorMetric = denominatorMetric;
        }


        @Override
        @Nullable
        public Double getValue(MetricsResult result, Map<Class<? extends Metric>, Metric> metrics, String target) {
            final Double numerator = result.getValueForMetric(metrics.get(numeratorMetric), target);
            final Double denominator = result.getValueForMetric(metrics.get(denominatorMetric), target);
            if (numerator == null || denominator == null) {
                return null;
            }
            return Double.valueOf(numerator.doubleValue() / denominator.doubleValue());
        }
    }
}
