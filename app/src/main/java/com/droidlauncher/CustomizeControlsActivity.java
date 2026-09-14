package com.droidlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class CustomizeControlsActivity extends Activity {
    private FrameLayout editor;
    private LinearLayout propertyPanel;
    private TextView selectedStatus;
    private TouchControlStore store;
    private final List<TouchControlConfig> controls = new ArrayList<>();
    private TouchControlConfig selected;
    private float downX;
    private float downY;
    private float startViewX;
    private float startViewY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(5, 6, 10));
        getWindow().setNavigationBarColor(Color.rgb(5, 6, 10));
        store = new TouchControlStore(this);
        controls.addAll(store.load());
        buildUi();
        editor.post(this::renderControls);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 18, 22, 18);
        root.setBackgroundColor(Color.rgb(7, 10, 15));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText("CUSTOMIZE CONTROLS");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, 60, 1));
        header.addView(actionButton("SAVE", v -> saveAndFinish()), fixed(110, 56));
        header.addView(actionButton("RESET", v -> resetLayout()), withLeft(fixed(110, 56), 8));
        header.addView(actionButton("+ ADD CONTROL", v -> showAddDialog()), withLeft(fixed(170, 56), 8));
        root.addView(header);

        selectedStatus = new TextView(this);
        selectedStatus.setTextColor(Color.rgb(185, 205, 225));
        selectedStatus.setTextSize(12);
        selectedStatus.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(selectedStatus, new LinearLayout.LayoutParams(-1, 40));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        editor = new FrameLayout(this);
        editor.setBackgroundColor(Color.rgb(20, 26, 35));
        LinearLayout.LayoutParams editorParams = new LinearLayout.LayoutParams(0, -1, 1);
        body.addView(editor, editorParams);

        propertyPanel = new LinearLayout(this);
        propertyPanel.setOrientation(LinearLayout.VERTICAL);
        propertyPanel.setPadding(16, 16, 16, 16);
        GradientDrawable propsBg = new GradientDrawable();
        propsBg.setColor(Color.rgb(13, 18, 25));
        propsBg.setCornerRadius(18f);
        propsBg.setStroke(1, Color.argb(90, 255, 255, 255));
        propertyPanel.setBackground(propsBg);
        LinearLayout.LayoutParams propertyParams = new LinearLayout.LayoutParams(290, -1);
        propertyParams.leftMargin = 14;
        body.addView(propertyPanel, propertyParams);
        root.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));

        Button done = actionButton("DONE", v -> saveAndFinish());
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(180, 54);
        doneParams.gravity = Gravity.CENTER_HORIZONTAL;
        doneParams.topMargin = 10;
        root.addView(done, doneParams);
        setContentView(root);
        showEmptyProperties();
    }

    private void renderControls() {
        editor.removeAllViews();
        int w = Math.max(1, editor.getWidth());
        int h = Math.max(1, editor.getHeight());
        for (TouchControlConfig config : controls) {
            if (!config.visible) continue;
            TextView chip = new TextView(this);
            chip.setText(config.label);
            chip.setTextColor(Color.WHITE);
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(11);
            chip.setAlpha(Math.max(0.15f, Math.min(1f, config.opacity)));
            GradientDrawable bg = shape(config.shape);
            chip.setBackground(bg);
            chip.setTag(config.id);
            float px = config.x * w;
            float py = config.y * h;
            int cw = Math.max(54, (int) (config.width * w));
            int ch = Math.max(44, (int) (config.height * h));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(cw, ch);
            editor.addView(chip, lp);
            chip.setX(clamp(px, 0, Math.max(0, w - cw)));
            chip.setY(clamp(py, 0, Math.max(0, h - ch)));
            chip.setOnClickListener(v -> select(config));
            chip.setOnTouchListener((v, event) -> handleDrag(v, event, config));
        }
        if (selected != null) select(findById(selected.id));
    }

    private boolean handleDrag(View v, MotionEvent event, TouchControlConfig config) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                select(config);
                downX = event.getRawX();
                downY = event.getRawY();
                startViewX = v.getX();
                startViewY = v.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float nx = startViewX + event.getRawX() - downX;
                float ny = startViewY + event.getRawY() - downY;
                v.setX(clamp(nx, 0, Math.max(0, editor.getWidth() - v.getWidth())));
                v.setY(clamp(ny, 0, Math.max(0, editor.getHeight() - v.getHeight())));
                config.x = v.getX() / Math.max(1f, editor.getWidth());
                config.y = v.getY() / Math.max(1f, editor.getHeight());
                updateSelectedStatus();
                return true;
            case MotionEvent.ACTION_UP:
                v.performClick();
                return true;
            default:
                return true;
        }
    }

    private void select(TouchControlConfig config) {
        if (config == null) return;
        selected = config;
        updateSelectedStatus();
        showProperties(config);
        for (int i = 0; i < editor.getChildCount(); i++) {
            View child = editor.getChildAt(i);
            child.setScaleX(child.getTag() != null && config.id.equals(child.getTag()) ? 1.08f : 1f);
            child.setScaleY(child.getTag() != null && config.id.equals(child.getTag()) ? 1.08f : 1f);
        }
    }

    private void showProperties(TouchControlConfig config) {
        propertyPanel.removeAllViews();
        TextView heading = label(config.label + "  •  " + config.action.getLabel());
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        propertyPanel.addView(heading);

        addText("Position: " + percent(config.x) + ", " + percent(config.y));
        addText("Size: " + percent(config.width) + " × " + percent(config.height));

        TextView opacityLabel = label("Opacity  " + Math.round(config.opacity * 100) + "%");
        propertyPanel.addView(opacityLabel);
        SeekBar opacity = new SeekBar(this);
        opacity.setMax(100);
        opacity.setProgress(Math.round(config.opacity * 100));
        opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                config.opacity = Math.max(0.15f, value / 100f);
                opacityLabel.setText("Opacity  " + value + "%");
                renderControls();
            }
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onStopTrackingTouch(SeekBar bar) {}
        });
        propertyPanel.addView(opacity);

        Spinner action = spinner(TouchControlAction.values(), config.action.ordinal());
        propertyPanel.addView(label("Action"));
        propertyPanel.addView(action);
        action.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                config.action = TouchControlAction.values()[pos];
                config.label = config.action.getLabel().toUpperCase(Locale.US);
                heading.setText(config.label + "  •  " + config.action.getLabel());
                renderControls();
            }
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });

        Spinner shape = spinner(new String[]{"circle", "rounded", "square"}, shapeIndex(config.shape));
        propertyPanel.addView(label("Shape"));
        propertyPanel.addView(shape);
        shape.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                config.shape = (String) p.getItemAtPosition(pos);
                renderControls();
            }
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });

        Button visibility = actionButton(config.visible ? "HIDE CONTROL" : "SHOW CONTROL", v -> {
            config.visible = !config.visible;
            renderControls();
            showProperties(config);
        });
        propertyPanel.addView(visibility, withTop(fixed(-1, 52), 12));

        Button duplicate = actionButton("DUPLICATE", v -> duplicate(config));
        propertyPanel.addView(duplicate, withTop(fixed(-1, 52), 8));
        Button delete = actionButton("DELETE", v -> delete(config));
        propertyPanel.addView(delete, withTop(fixed(-1, 52), 8));
    }

    private void showEmptyProperties() {
        propertyPanel.removeAllViews();
        propertyPanel.addView(label("Select a control"));
        propertyPanel.addView(label("Drag controls to move them. Use the panel to change action, opacity, shape and visibility."));
    }

    private void showAddDialog() {
        String[] labels = new String[TouchControlAction.values().length];
        for (int i = 0; i < labels.length; i++) labels[i] = TouchControlAction.values()[i].getLabel();
        new AlertDialog.Builder(this).setTitle("Add Control").setItems(labels, (d, which) -> {
            TouchControlAction action = TouchControlAction.values()[which];
            controls.add(new TouchControlConfig(UUID.randomUUID().toString(), action.getLabel().toUpperCase(Locale.US),
                    action, 0.42f, 0.42f, 0.12f, 0.15f, 0.82f, "rounded", true));
            renderControls();
        }).show();
    }

    private void duplicate(TouchControlConfig config) {
        TouchControlConfig copy = config.copy();
        copy.id = UUID.randomUUID().toString();
        copy.x = Math.min(0.88f, copy.x + 0.05f);
        copy.y = Math.min(0.82f, copy.y + 0.05f);
        controls.add(copy);
        renderControls();
        select(copy);
    }

    private void delete(TouchControlConfig config) {
        controls.remove(config);
        selected = null;
        renderControls();
        showEmptyProperties();
        Toast.makeText(this, "Control deleted", Toast.LENGTH_SHORT).show();
    }

    private void resetLayout() {
        store.reset();
        controls.clear();
        controls.addAll(store.load());
        selected = null;
        renderControls();
        showEmptyProperties();
        Toast.makeText(this, "Controls reset", Toast.LENGTH_SHORT).show();
    }

    private void saveAndFinish() {
        store.save(controls);
        Toast.makeText(this, "Control layout saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private TouchControlConfig findById(String id) {
        for (TouchControlConfig c : controls) if (c.id.equals(id)) return c;
        return null;
    }

    private GradientDrawable shape(String value) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(120, 65, 95, 125));
        bg.setStroke(2, Color.argb(170, 210, 230, 250));
        if ("circle".equals(value)) bg.setCornerRadius(1000f);
        else if ("rounded".equals(value)) bg.setCornerRadius(22f);
        else bg.setCornerRadius(4f);
        return bg;
    }

    private Spinner spinner(Object[] values, int selection) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(this,
                android.R.layout.simple_spinner_dropdown_item, values);
        spinner.setAdapter(adapter);
        spinner.setSelection(Math.max(0, selection));
        return spinner;
    }

    private TextView label(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(Color.WHITE);
        t.setTextSize(13);
        t.setPadding(0, 6, 0, 6);
        return t;
    }

    private void addText(String text) { propertyPanel.addView(label(text)); }
    private String percent(float value) { return Math.round(value * 100f) + "%"; }
    private int shapeIndex(String value) { return Arrays.asList("circle", "rounded", "square").indexOf(value); }
    private float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
    private void updateSelectedStatus() {
        selectedStatus.setText(selected == null ? "Drag controls to position them. Select one to edit." :
                selected.label + "  •  x=" + percent(selected.x) + " y=" + percent(selected.y));
    }
    private Button actionButton(String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text); b.setTextColor(Color.WHITE); b.setTextSize(12); b.setAllCaps(false); b.setStateListAnimator(null);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.rgb(29, 40, 52)); bg.setCornerRadius(18f); bg.setStroke(1, Color.argb(120, 200, 225, 245)); b.setBackground(bg);
        b.setOnClickListener(listener); return b;
    }
    private LinearLayout.LayoutParams fixed(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    private LinearLayout.LayoutParams withLeft(LinearLayout.LayoutParams p, int margin) { p.leftMargin = margin; return p; }
    private LinearLayout.LayoutParams withTop(LinearLayout.LayoutParams p, int margin) { p.topMargin = margin; return p; }
}
