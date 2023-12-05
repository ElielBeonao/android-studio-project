package ca.uottawa.testnovigrad.fwk;

import java.util.List;

public interface OnMultiSelectListener<T> {

    void onMultiSelect(List<T> selectedRows);
}
