package com.blues.smallestwidthapp.ui;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.blues.smallestwidthapp.R;

public class CodePop extends PopupWindow {
    private ImageView imageView;
    private TextView codeText;

    public void setImageBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public void setCodeText(String text) {
        codeText.setText(text);
    }

    public CodePop(Context context) {
        super();
        setClippingEnabled(true);
        setFocusable(true);
        setHeight(MATCH_PARENT);
        setWidth(MATCH_PARENT);
        View view = LayoutInflater.from(context).inflate(R.layout.pop_content, null, false);
        bindView(view);
        setContentView(view);
    }

    private void bindView(View view) {
        imageView = view.findViewById(R.id.code_image);
        codeText = view.findViewById(R.id.id_code_text);
    }

    public void showPop(View view) {
        showAtLocation(view, Gravity.CENTER, 0, 0);
    }
}
