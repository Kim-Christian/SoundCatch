package com.soundcatch.soundcatch;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Kim-Christian on 2017-04-13.
 */

public class ProjectCard {

    private CardView cardView;
    private Project project;
    private String projectName;
    private Drawable cardBackground;
    private Drawable textBackground;
    private Drawable highlightTextBackground;
    private TextView textView;
    private int cardHeight;
    private int margin;
    private DisplayMetrics displayMetrics;
    private static final int LAYOUT = R.layout.cardview_project;
    private static final int ID = R.id.card_view;
    private boolean onCard = false;
    private OnDropListener mCallback;

    public ProjectCard(Context context, Project project/*, Drawable cardBackground*/) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CardView v = (CardView) vi.inflate(LAYOUT, null);
        this.cardView = (CardView) v.findViewById(ID);
        this.project = project;
        this.projectName = project.getName();
        this.cardBackground = project.getBackground();
        this.textBackground = context.getDrawable(R.drawable.smoke);
        this.highlightTextBackground = context.getDrawable(R.drawable.smoke_green);
        this.textView = (TextView) v.findViewById(R.id.card_project_name);
        this.displayMetrics = context.getResources().getDisplayMetrics();
        this.cardHeight = dpToPx(context.getResources().getInteger(R.integer.large_card_height));
        this.margin = dpToPx(context.getResources().getInteger(R.integer.card_margin));
        applyToCard();
        myDragEventListener mDragListen = new myDragEventListener();
        cardView.setOnDragListener(mDragListen);
        try {
            mCallback = (ProjectCard.OnDropListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Listener");
        }
    }

    public CardView getCardView() {
        return cardView;
    }
    public TextView getTextView() {
        return textView;
    }

    private void applyToCard() {
        LinearLayout.LayoutParams cardLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, cardHeight);
        cardLP.setMargins(margin, margin, margin, 0);
        cardView.setBackground(cardBackground);
        cardView.setLayoutParams(cardLP);
        textView.setText(projectName);
    }

    public void adjustTextView() {
        int width = textView.getWidth();
        int height = textView.getHeight();
        RelativeLayout.LayoutParams textLP = new RelativeLayout.LayoutParams(width * 2, height);
        textView.setBackground(textBackground);
        textLP.setMarginStart(100 - width/2);
        textView.setLayoutParams(textLP);
    }

    private int dpToPx(float dp) {
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public interface OnDropListener {
        public void onDropListener(Project project);
    }

    protected class myDragEventListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {

            final int action = event.getAction();
            switch(action) {

                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    onCard = true;
                    textView.setBackground(highlightTextBackground);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    onCard = false;
                    textView.setBackground(textBackground);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    if (onCard) {
                        System.out.println("OnDropInCard!!!!!!!!!!!!!");
                        mCallback.onDropListener(project);
                        onCard = false;
                        System.out.println("onCard done");
                    }
                    textView.setBackground(textBackground);
                    System.out.println("set color");
                    return true;

                default:
                    System.out.println("action");
                    break;
            }
            return false;
        }
    }
}
