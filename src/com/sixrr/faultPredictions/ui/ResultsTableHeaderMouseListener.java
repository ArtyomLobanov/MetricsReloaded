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

package com.sixrr.faultPredictions.ui;

import com.intellij.ui.table.JBTable;

import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class ResultsTableHeaderMouseListener extends MouseAdapter {
    private final JBTable table;

    ResultsTableHeaderMouseListener(JBTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        final JTableHeader tableHeader = table.getTableHeader();
        final Point point = new Point(e.getX(), e.getY());
        final int column = tableHeader.columnAtPoint(point);
        if (column == -1) {
            return;
        }
        if (table.getModel() instanceof ResultsTableModel && e.getButton() == MouseEvent.BUTTON1) {
            final ResultsTableModel tableModel = (ResultsTableModel) table.getModel();
            final int sortColumn = tableModel.getSortColumn();
            if (sortColumn == column) {
                final boolean ascending = tableModel.isAscending();
                tableModel.changeSort(sortColumn, !ascending);
            } else {
                tableModel.changeSort(column, true);
            }
            tableHeader.repaint();

        }
        table.setColumnSelectionInterval(column, column);
        table.repaint();
    }
}
