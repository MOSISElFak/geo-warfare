package rs.elfak.jajac.geowarfare.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;


public class NumTextView extends android.support.v7.widget.AppCompatTextView {

    public NumTextView(Context context) {
        super(context);
    }

    public NumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        String numText = text.toString();
        if (numText.isEmpty()) {
            numText = String.valueOf(0);
        } else {
            numText = Num2Str.convert(Integer.valueOf(numText));
        }
        super.setText(numText, type);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
