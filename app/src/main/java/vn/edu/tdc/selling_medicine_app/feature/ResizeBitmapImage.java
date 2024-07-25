package vn.edu.tdc.selling_medicine_app.feature;

import android.graphics.Bitmap;

public class ResizeBitmapImage {
    public Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        float aspectRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();

        int newWidth = width;
        int newHeight = (int) (width / aspectRatio);

        if (newHeight > height) {
            newHeight = height;
            newWidth = (int) (height * aspectRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}
