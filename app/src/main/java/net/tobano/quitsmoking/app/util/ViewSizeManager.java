package net.tobano.quitsmoking.app.util;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.TextView;

/**
 * Created by Carine on 20/07/16.
 */
public class ViewSizeManager {

    public static int MIN_TEXT_SIZE = 8;

    public static int textFitWithTextview(Context context, int textViewWidth, int textViewHeight, String text){

        System.out.println(" ");
        System.out.println("-"+text+"-");
        System.out.println("largeur : " + String.valueOf(textViewWidth));
        System.out.println("hauteur : " + String.valueOf(textViewHeight));

        int textSize = MIN_TEXT_SIZE;
        System.out.println("text size : " + String.valueOf(textSize));

        Rect rect = createRectFromText(context, text, textSize);
        System.out.println("width : " + String.valueOf(rect.width()));
        System.out.println("height : " + String.valueOf(rect.height()));

        if((rect.width() > textViewWidth) || (rect.height() > textViewHeight)) {
            while ((rect.width() > textViewWidth) || (rect.height() > textViewHeight)) {
                textSize--;
                rect = createRectFromText(context, text, textSize);
                System.out.println("text size : " + String.valueOf(textSize));
                System.out.println("width : " + String.valueOf(rect.width()));
                System.out.println("height : " + String.valueOf(rect.height()));
            }
        }
        else {
            while ((rect.width() < textViewWidth) && (rect.height() < textViewHeight)) {
                textSize++;
                rect = createRectFromText(context, text, textSize);
            }
            textSize--;
        }

        System.out.println("text size : " + String.valueOf(textSize));
        textSize += 2; // TODO : why +2 is better ?
        return textSize;
    }

    private static Rect createRectFromText(Context context, String text, int size) {

        Rect bounds = new Rect();
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(size);
        Paint paint = textView.getPaint();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }
}
