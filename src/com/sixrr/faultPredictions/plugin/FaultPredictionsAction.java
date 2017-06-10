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

package com.sixrr.faultPredictions.plugin;


import com.intellij.analysis.AnalysisScope;
import com.intellij.analysis.BaseAnalysisAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.sixrr.faultPredictions.classification.FaultPredictor;
import com.sixrr.faultPredictions.model.AnalyzedEntity;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsExecutionContextImpl;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import com.sixrr.metrics.utils.MetricsReloadedBundle;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;
import org.jetbrains.annotations.NotNull;

import static com.intellij.notification.NotificationType.ERROR;
import static com.intellij.notification.NotificationType.INFORMATION;

public class FaultPredictionsAction extends BaseAnalysisAction {
    private static final String NOTIFICATIONS_GROUP = "Fault predicting";
    private static final String SUCCEED_TITLE = "Fault prediction succeed";
    private static final String ERROR_TITLE = "Fault prediction failed";
    private static final String ERROR_TEXT_TEMPLATE = "Error occurred during %s. Message:\n%s";

    public FaultPredictionsAction() {
        super(MetricsReloadedBundle.message("fault.predictions"), MetricsReloadedBundle.message("metrics"));
    }

    @Override
    protected void analyze(@NotNull final Project project, @NotNull final AnalysisScope analysisScope) {
        final MetricsProfileRepository repository = MetricsProfileRepository.getInstance();
        final MetricsProfile profile =
                repository.getProfileForName(StockMetricsBundle.message("fault.predictions.metrics.profile.name"));
        final MetricsRun metricsRun = new MetricsRunImpl();
        new MetricsExecutionContextImpl(project, analysisScope) {

            @Override
            public void onFinish() {
                final FaultPredictor faultPredictor;
                try {
                    faultPredictor = FaultPredictor.loadDefaultPredictor();
                } catch (Exception e) {
                    showNotification(String.format(ERROR_TEXT_TEMPLATE, "predictor loading", e.getMessage()), ERROR);
                    return;
                }
                final AnalyzedEntity[] analyzedEntities;
                try {
                    analyzedEntities = faultPredictor.analyze(metricsRun.getResultsForCategory(MetricCategory.Method));
                } catch (Exception e) {
                    showNotification(String.format(ERROR_TEXT_TEMPLATE, "data analyzing", e.getMessage()), ERROR);
                    return;
                }
                //todo use normal way to show results!!
                for (AnalyzedEntity entity : analyzedEntities) {
                    showNotification(entity.getName() + " " + entity.isDefective(), INFORMATION);
                }
                showNotification("Analysis completed", INFORMATION);
            }
        }.execute(profile, metricsRun);
    }

    private static void showNotification(String message, NotificationType type) {
        final String title = type == ERROR? ERROR_TITLE : SUCCEED_TITLE;
        final Notification notification = new Notification(NOTIFICATIONS_GROUP, title, message, type);
        Notifications.Bus.notify(notification);
    }
}
