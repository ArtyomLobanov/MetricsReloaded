/*
 * Copyright 2005-2016 Sixth and Red River Software, Bas Leijdekkers
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

package com.sixrr.faultPredictions.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.content.Content;
import com.intellij.ui.table.JBTable;
import com.sixrr.faultPredictions.model.AnalyzedEntity;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class FaultPredictionsToolWindow implements Disposable {

    private static final String WINDOW_ID = "Fault predictions window";

    private final Project project;
    private ToolWindow myToolWindow = null;
    private ResultsTableModel tableModel;


    private FaultPredictionsToolWindow(@NotNull Project project) {
        this.project = project;
        register();
        createTable();
    }

    private void register() {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        myToolWindow = toolWindowManager.registerToolWindow(WINDOW_ID, true, ToolWindowAnchor.BOTTOM);
        myToolWindow.setTitle("Fault predictions result");
        myToolWindow.setAvailable(false, null);
    }

    private void createTable() {
        tableModel = new ResultsTableModel();
        final JBTable table = new JBTable();
        table.setModel(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().addMouseListener(new ResultsTableHeaderMouseListener(table));
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer(tableModel, SwingConstants.LEFT));
        final JComponent component = ScrollPaneFactory.createScrollPane(table);
        final Content content = myToolWindow.getContentManager().getFactory()
                .createContent(component, "Fault predictions table", true);
        new TableSpeedSearch(table);
        myToolWindow.getContentManager().addContent(content);

    }

    public void show(AnalyzedEntity[] data) {
        myToolWindow.setTitle("Fault predictions result");
        myToolWindow.setAvailable(true, null);
        tableModel.updateResults(data);
        myToolWindow.show(null);
    }

    @Override
    public void dispose() {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow(WINDOW_ID);
    }
}
