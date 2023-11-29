package ca.uottawa.testnovigrad.fwk;

import ca.uottawa.testnovigrad.models.User;

public interface OnUserEditButtonClickListener {

    void onEditButtonClick(User user);

    void onDeleteButtonClick(User user);
}
