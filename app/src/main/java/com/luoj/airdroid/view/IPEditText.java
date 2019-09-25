package com.luoj.airdroid.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.elvishew.xlog.XLog;
import com.luoj.airdroid.R;

import java.util.ArrayList;
import java.util.List;

public class IPEditText extends LinearLayout implements TextWatcher {
    private static final String TAG = "IPEditText";

    private int width;
    private int height;
    private Paint paint;

    private static final int DEFAULT_TEXT_MAX_LENGTH = 3;
    private static final int DEFAULT_TEXT_SIZE = 16;
    private static final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
    private static final int DEFAULT_BORDER_WIDTH = 2;
    private static final int DEFAULT_POINT_COLOR = Color.BLACK;
    private static final int DEFAULT_POINT_WIDTH = 5;
    private static final int DEFAULT_IP_EDITTEXT_LENGTH = 4;

    private int textLength;
    private int textSize;
    private int textColor;

    private int borderColor;
    private int borderWidth;

    private int pointColor;
    private int pointWidth;
    private int editNumber;


    private int default_height = px2dp(20);
    private int default_width = px2dp(60);

    private List<EditText> data = new ArrayList<>();

    public IPEditText(Context context) {
        this(context, null);
    }

    public IPEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IPEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IPEditText, defStyleAttr, 0);
        textLength = ta.getInt(R.styleable.IPEditText_textLength, DEFAULT_TEXT_MAX_LENGTH);

        textSize = (int) ta.getDimension(R.styleable.IPEditText_textSize, DEFAULT_TEXT_SIZE);
        textColor = ta.getColor(R.styleable.IPEditText_textColor, DEFAULT_TEXT_COLOR);

        borderColor = ta.getColor(R.styleable.IPEditText_borderColor, DEFAULT_BORDER_COLOR);
        borderWidth = (int) ta.getDimension(R.styleable.IPEditText_borderWidth, DEFAULT_BORDER_WIDTH);

        pointColor = ta.getColor(R.styleable.IPEditText_pointColor, DEFAULT_POINT_COLOR);
        pointWidth = (int) ta.getDimension(R.styleable.IPEditText_pointWidth, DEFAULT_POINT_WIDTH);

        editNumber = ta.getInt(R.styleable.IPEditText_editNumber, DEFAULT_IP_EDITTEXT_LENGTH);

        init(context);
        initPaint();
    }

    public float getTextSize() {
        return data.get(0).getTextSize();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
    }


    private void init(Context context) {
        for (int i = 0; i < editNumber; i++) {
            EditText edit = new EditText(context);
            edit.setBackground(null);
            edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(textLength)});
            edit.setTextSize(textSize);
            edit.setTextColor(textColor);
            edit.setGravity(Gravity.CENTER);
            edit.setInputType(InputType.TYPE_CLASS_NUMBER);
            edit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
            edit.setMinHeight(default_height);
            edit.setMinWidth(default_width);
            edit.setTag(i);
            edit.setMaxLines(1);
            edit.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
            edit.addTextChangedListener(this);
            addView(edit);
            data.add(edit);
        }

        setBackgroundColor(getResources().getColor(android.R.color.transparent));
        setDividerDrawable(getResources().getDrawable(android.R.drawable.divider_horizontal_textfield));
        setOrientation(LinearLayout.HORIZONTAL);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int l = data.get(0).getLeft();
        int t = data.get(0).getTop() - getPaddingTop();
        int r = width - getPaddingRight();
        int b = height;

        Rect rect = new Rect(l, t, r, b);

        paint.setColor(borderColor);
        paint.setStrokeWidth(borderWidth);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect, paint);

        int y = height / 2;
        int x = width / editNumber;
        paint.setStrokeWidth(pointWidth);
        paint.setColor(pointColor);
        for (int i = 1; i < data.size(); i++) {
            canvas.drawPoint(x * i, y, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (listener != null) {
            listener.afterTextChanged(getEditTextValue());
        }
        if (s.length() == 3) {
            for (int i = 0; i < data.size(); i++) {
                EditText edit = data.get(i);
                String val = edit.getText().toString();
                if (val.length() == 0) {
                    edit.requestFocus();
                    return;
                }
            }
        } else if (s.length() == 0) {
            for (int i = data.size() - 1; i >= 0; i--) {
                EditText edit = data.get(i);
                edit.setFocusable(true);
                String val = edit.getText().toString();
                if (val.length() == 3) {
                    edit.requestFocus();
                    edit.setSelection(3);
                    return;
                }
            }
        }
    }

    public String[] getEditTextValue() {
        String[] val = new String[editNumber];
        for (int i = 0; i < editNumber; i++) {
            val[i] = data.get(i).getText().toString();
            XLog.d(data.get(i).getText().toString());
        }
        return val;
    }

    public String getText() {
        String[] strings = getEditTextValue();
        if (null != strings && strings.length == 4 &&
                !TextUtils.isEmpty(strings[0]) &&
                !TextUtils.isEmpty(strings[1]) &&
                !TextUtils.isEmpty(strings[2]) &&
                !TextUtils.isEmpty(strings[3])
        ) {
            return strings[0] + "." + strings[1] + "." + strings[2] + "." + strings[3];
        }
        return null;
    }

    public void setEdittextValue(String[] s) {
        for (int i = 0; i < s.length; i++) {
            data.get(i).setText(s[i]);
        }
    }

    public boolean getCompile() {
        for (int i = 0; i < editNumber; i++) {
            String str = data.get(i).getText().toString();
            if (Integer.parseInt(str) <= 255) {
                return true;
            }
        }
        return false;
    }

    public int px2dp(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }

    public int px2sp(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, val, getResources().getDisplayMetrics());
    }

    public interface IPTextWatcher {
        public void afterTextChanged(String[] s);
    }

    private IPTextWatcher listener;

    public void setTextWatcher(IPTextWatcher listener) {
        this.listener = listener;
    }

}