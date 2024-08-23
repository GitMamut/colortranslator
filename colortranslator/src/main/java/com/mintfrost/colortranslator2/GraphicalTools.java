package com.mintfrost.colortranslator2;

import android.graphics.Color;

public class GraphicalTools {
   public static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        int heightStart = height/2 - 1;
        int heightStop = height/2;
        int widthStart = width/2 - 2;
        int widthStop = width/2 - 1;
        int ypStart = heightStart * width + widthStart;

        for (int j = heightStart, yp = ypStart; j < heightStop; j++) {
            int uvp = frameSize + (j >> 1) * width + widthStart, u = 0, v = 0;
            for (int i = widthStart; i < widthStop; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;

                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;

                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                rgb[0] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    public static String getHexText(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    public static String getRgbText(int color) {
        return String.format("%d, %d, %d", Color.red(color), Color.green(color), Color.blue(color));
    }
}
