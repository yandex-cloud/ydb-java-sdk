package ru.yandex.ydb.examples;

import java.io.IOException;

import ru.yandex.ydb.table.result.ResultSetReader;


/**
 * @author Sergey Polovko
 */
public class TablePrinter {

    private final ResultSetReader reader;
    private final int[] columnMaxWidth;

    public TablePrinter(ResultSetReader reader) {
        this.reader = reader;
        this.columnMaxWidth = new int[reader.getColumnCount()];

        for (int i = 0; i < reader.getColumnCount(); i++) {
            columnMaxWidth[i] = Math.max(columnMaxWidth[i], reader.getColumnName(i).length());
        }
        while (reader.next()) {
            for (int i = 0; i < reader.getColumnCount(); i++) {
                columnMaxWidth[i] = Math.max(columnMaxWidth[i], reader.getColumn(i).toString().length());
            }
        }
        reader.setRowIndex(0);
    }

    public void print() {
        print(System.out);
    }

    private void print(Appendable out) {
        writeDivider(out);

        // (1) header
        write(out, "| ");
        for (int i = 0; i < reader.getColumnCount(); i++) {
            if (i > 0) {
                write(out, " | ");
            }
            writePadded(out, i, reader.getColumnName(i));
        }
        write(out, " |\n");
        writeDivider(out);

        // (2) rows
        while (reader.next()) {
            write(out, "| ");
            for (int i = 0; i < reader.getColumnCount(); i++) {
                if (i > 0) {
                    write(out, " | ");
                }
                writePadded(out, i, reader.getColumn(i).toString());
            }
            write(out, " |\n");
        }
        writeDivider(out);
    }

    private void writeDivider(Appendable out) {
        write(out, "+-");
        for (int i = 0; i < columnMaxWidth.length; i++) {
            if (i > 0) {
                write(out, "-+-");
            }
            for (int j = 0; j < columnMaxWidth[i]; j++) {
                write(out, '-');
            }
        }
        write(out, "-+\n");
    }

    private void write(Appendable out, char ch) {
        try {
            out.append(ch);
        } catch (IOException e) {
            throw new RuntimeException("cannot output \'" + ch + '\'', e);
        }
    }

    private void write(Appendable out, String value) {
        try {
            out.append(value);
        } catch (IOException e) {
            throw new RuntimeException("cannot output \'" + value + '\"', e);
        }
    }

    private void writePadded(Appendable out, int columnIndex, String value) {
        try {
            for (int i = 0, len = columnMaxWidth[columnIndex] - value.length(); i < len; i++) {
                out.append(' ');
            }
            out.append(value);
        } catch (IOException e) {
            throw new RuntimeException("cannot output \"" + value + '\"', e);
        }
    }
}
