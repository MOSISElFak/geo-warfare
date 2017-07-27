package rs.elfak.jajac.geowarfare.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class MaxValueTextWatcher implements TextWatcher {

    private EditText editText;
    private int max;

    public MaxValueTextWatcher(EditText editText, int max) {
        this.editText = editText;
        this.max = max;
    }

    public void setMax(int newMax) {
        this.max = newMax;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String text = s.toString();

        if (text.length() > 0) {
            if (!isInt(text)) {
                editText.setText(null);
            } else {
                int value = Integer.parseInt(text);
                if (value == 0) {
                    editText.setText(null);
                } else if (value > max) {
                    editText.setText(String.valueOf(max));
                    editText.setSelection(editText.length());
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) { }

    private boolean isInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
