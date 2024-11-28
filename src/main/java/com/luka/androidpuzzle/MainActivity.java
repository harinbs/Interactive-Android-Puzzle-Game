package com.luka.androidpuzzle;

import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private List<LinearLayout> linearLayouts = new ArrayList<>();
    private List<Integer> imageResourceIds = new ArrayList<>();
    private int moveCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Puzzle Game");

        gridLayout = findViewById(R.id.gridLayout);
        initializeGame();

        Toast.makeText(this, "Welcome to the Puzzle Game!", Toast.LENGTH_SHORT).show();
    }

    private void initializeGame() {
        gridLayout.removeAllViews();
        linearLayouts.clear();
        imageResourceIds.clear();

        // Create the grid layout and image resources
        for (int i = 0; i <= 3; i++) {
            for (int j = 0; j <= 2; j++) {
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayouts.add(linearLayout);

                linearLayout.setOnDragListener(new MyDragListener());

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f),
                        GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                );
                params.setMargins(3, 3, 3, 3);
                linearLayout.setLayoutParams(params);
                linearLayout.setGravity(Gravity.CENTER);

                ImageView imageView = new ImageView(this);
                imageView.setId(View.generateViewId());
                imageResourceIds.add(imageView.getId());

                imageView.setOnTouchListener(new MyTouchListener());

                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                params2.gravity = Gravity.CENTER;
                imageView.setLayoutParams(params2);
                imageView.setPadding(3, 3, 3, 3);
                imageView.setImageResource(getResources().getIdentifier(
                        "android" + i + j, "drawable", getPackageName()));
                imageView.setAdjustViewBounds(true);
                linearLayout.addView(imageView);
                gridLayout.addView(linearLayout);
            }
        }

        shufflePuzzle();
    }

    private void shufflePuzzle() {
        Collections.shuffle(imageResourceIds);

        for (int i = 0; i < linearLayouts.size(); i++) {
            ImageView imageView = findViewById(imageResourceIds.get(i));

            if (imageView == null) continue;

            ViewParent parent = imageView.getParent();
            if (parent instanceof ViewGroup) ((ViewGroup) parent).removeView(imageView);

            linearLayouts.get(i).addView(imageView);
        }
    }

    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() != MotionEvent.ACTION_DOWN) return false;

            moveCount++;
            view.startDragAndDrop(null, new View.DragShadowBuilder(view), view, 0);

            return true;
        }
    }

    private final class MyDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) draggedView.getParent();

                    if (owner != null) {
                        owner.removeView(draggedView);

                        if (v instanceof LinearLayout) {
                            LinearLayout container = (LinearLayout) v;

                            if (container.getChildCount() > 0) {
                                View oldView = container.getChildAt(0);
                                container.removeViewAt(0);

                                owner.addView(oldView);
                            }

                            container.addView(draggedView);
                            checkIfSolved();
                        }
                    }
                    break;
            }
            return true;
        }
    }

    private void checkIfSolved() {
        boolean isSolved = true;

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View childView = gridLayout.getChildAt(i);

            if (!(childView instanceof LinearLayout)) continue;

            LinearLayout linearLayout = (LinearLayout) childView;

            if (linearLayout.getChildCount() != 1) {
                isSolved = false;
                break;
            }

            View innerChildView = linearLayout.getChildAt(0);

            if (!(innerChildView instanceof ImageView)) {
                isSolved = false;
                break;
            }

            ImageView imageView = (ImageView) innerChildView;

            if (imageView.getId() != imageResourceIds.get(i)) {
                isSolved = false;
                break;
            }
        }

        if (isSolved) {
            Toast.makeText(MainActivity.this, "🎉 Congratulations! You solved the puzzle! 🧩", Toast.LENGTH_LONG).show();

            // Reset game after a delay
            gridLayout.postDelayed(this::resetGame, 2000);
        }
    }

    private void resetGame() {
        moveCount = 0;
        initializeGame();
    }
}
