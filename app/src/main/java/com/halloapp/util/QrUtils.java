package com.halloapp.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.halloapp.util.logs.Log;

public class QrUtils {

    private static final int QR_CODE_DIMENSION = 1000;

    private static Bitmap bitMatrixToBitmap(@NonNull BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixels[i*height + j] = bitMatrix.get(j, i) ? Color.BLACK : Color.WHITE;
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap encode(@NonNull String url) {
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_CODE_DIMENSION, QR_CODE_DIMENSION);
            return bitMatrixToBitmap(bitMatrix);
        } catch (Exception e) {
            Log.e("Failed to convert url to bitMatrix", e);
        }
        return null;
    }
}
