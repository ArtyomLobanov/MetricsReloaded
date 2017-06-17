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
import com.sixrr.metrics.profile.MetricsProfileRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.Comparator;
import java.util.stream.IntStream;

class ResultsTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"Method", "Is defect", "Comment"};
    private final AnalyzedEntity[] entities;
    private final int[] rowPermutation;
//    private int sortColumn = -1;
//    private boolean ascending;

    ResultsTableModel(@NotNull AnalyzedEntity[] entities) {
        this.entities = entities;
        rowPermutation = IntStream.range(0, entities.length).toArray();
        sort();
    }

    public void changeSort(int column, boolean ascending) {
//        sortColumn = column;
//        this.ascending = ascending;
//        sort();
//        fireTableDataChanged();
//        MetricsProfileRepository.getInstance().persistCurrentProfile();
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
        final AnalyzedEntity entity = entities[rowPermutation[rowIndex]];
        switch (columnIndex) {
            case 0 : return entity.getName();
            case 1 : return Double.toString(entity.isDefective());
            case 2 : return entity.getComment();
        }
        throw new IllegalArgumentException("Unexpected column index: " + columnIndex);
    }

    private void sort() {
//        if (sortColumn == 1) {
//            rowPermutation = IntStream.range(0, entities.length).boxed()
//                    .sorted(Comparator.comparing(i -> sortColumn == 0? entities[i].getName() : entities[i].getComment()))
//                    .mapToInt(Integer::intValue).toArray();
//        }
    }
}
