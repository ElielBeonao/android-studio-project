package ca.uottawa.testnovigrad.fwk;

import ca.uottawa.testnovigrad.models.User;

public interface OnEntityModelButtonClickListener<T> {

    void onEditButtonClick(T entity);

    void onDeleteButtonClick(T entity);
}
