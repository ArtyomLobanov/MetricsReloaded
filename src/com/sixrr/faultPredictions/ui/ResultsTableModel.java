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

import com.sixrr.faultPredictions.model.AnalyzedEntity;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.Comparator;

class ResultsTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Method", "Defectiveness", "Comment"};
    private AnalyzedEntity[] entities;
    private int sortColumn = 0;
    private boolean ascending = true;

    ResultsTableModel() {
        entities = new AnalyzedEntity[0];
    }

    public void updateResults(AnalyzedEntity[] entities) {
        this.entities = entities;
        sort();
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    public void changeSort(int column, boolean ascending) {
        sortColumn = column;
        this.ascending = ascending;
        sort();
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public int getRowCount() {
        return entities.length;
    }

    @Override
    @Nullable
    public Object getValueAt(int rowIndex, int columnIndex) {
        return toStringValue(entities[rowIndex], columnIndex);
    }

    public int getSortColumn() {
        return sortColumn;
    }

    public boolean isAscending() {
        return ascending;
    }

    private static String toStringValue(AnalyzedEntity entity, int column) {
        switch (column) {
            case 0:
                return entity.getName();
            case 1:
                return Integer.toString(entity.defectiveness());
            case 2:
                return entity.getComment();
        }
        throw new IllegalArgumentException("Unexpected column index: " + column);
    }

    private void sort() {
        final Comparator<AnalyzedEntity> mainComparator = Comparator.comparing(e -> toStringValue(e, sortColumn));
        if (ascending) {
            Arrays.sort(entities, mainComparator);
        } else {
            Arrays.sort(entities, mainComparator.reversed());
        }
    }
}
