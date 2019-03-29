package music.gatech.edu.concertstitch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mcw0805
 */
public class DrawBoxView extends View {
    List<String> labelList;

    public DrawBoxView(Context context) {
        super(context);
        labelList = new ArrayList<>();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPaint(paint);


        canvas.drawText("Some Text", 10, 25, paint);

    }

    public void setList(List<String> labelList) {
        this.labelList = labelList;
    }
}
