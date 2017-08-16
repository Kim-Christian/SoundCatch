package com.soundcatch.soundcatch;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Kim-Christian on 2017-04-13.
 */

public class RecordingCard {

    private CardView cardView;
    private String recordingName;
    private TextView textView;
    private int cardHeight;
    private int margin;
    private DisplayMetrics displayMetrics;
    private static final int LAYOUT = R.layout.cardview_recording;
    private static final int ID = R.id.card_view;
    private OnClickListener mClickCallback;

    public RecordingCard(Context context, final Recording recording) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CardView v = (CardView) vi.inflate(LAYOUT, null);
        this.cardView = (CardView) v.findViewById(ID);
        this.recordingName = recording.getName();
        this.textView = (TextView) v.findViewById(R.id.card_recording_name);
        this.displayMetrics = context.getResources().getDisplayMetrics();
        this.cardHeight = dpToPx(context.getResources().getInteger(R.integer.small_card_height));
        this.margin = dpToPx(context.getResources().getInteger(R.integer.card_margin));
        applyToCard();
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickCallback.onCardClick(recording);
            }
        });

        cardView.setTag(recording.getName());
        cardView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
                ClipData dragData = new ClipData((CharSequence) v.getTag(),
                        new String[]{ ClipDescription.MIMETYPE_TEXT_PLAIN }, item);
                View.DragShadowBuilder myShadow = new MyDragShadowBuilder(cardView);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    v.startDragAndDrop(dragData, myShadow, null, 0);
                } else {
                    v.startDrag(dragData, myShadow, null, 0);
                }
                return true;
            }
        });

        try {
            mClickCallback = (OnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement Listener");
        }
    }

    public CardView getCardView() {
        return cardView;
    }

    private void applyToCard() {
        LinearLayout.LayoutParams cardLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, cardHeight);
        cardLP.setMargins(margin, margin, margin, 0);
        //cardView.setBackground(cardBackground);
        cardView.setLayoutParams(cardLP);
        textView.setText(recordingName);
    }

    private int dpToPx(float dp) {
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public interface OnClickListener {
        public void onCardClick(Recording recording);
    }

    public interface OnDragListener {
        public void onDragStart(CardView view);
        public void onDragEnter(CardView view);
        public void onDragLocation(CardView view);
        public void onDragExit(CardView view);
        public void onDragDrop(CardView view, String dragData);
        public void onDragEnd(CardView view);
    }

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {
        private static Drawable shadow;
        public MyDragShadowBuilder(View v) {
            super(v);
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            int width, height;
            width = getView().getWidth();
            height = getView().getHeight();
            shadow.setBounds(0, 0, width, height);
            size.set(width, height);
            touch.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            shadow.draw(canvas);
        }
    }
}
