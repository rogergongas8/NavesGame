package com.example.navesgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SvgManager {
    private static final String TAG = "SvgManager";
    private SVG sheetSvg;
    private final Map<String, SpriteRegion> spriteRegions = new HashMap<>();
    private final Map<String, Bitmap> bitmapCache = new HashMap<>();
    private final Context context;

    private static class SpriteRegion {
        float x, y, width, height;

        SpriteRegion(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public SvgManager(Context context) {
        this.context = context;
        loadAssets();
    }

    private void loadAssets() {
        try {
            // Load SVG
            InputStream svgInputStream = context.getAssets().open("sheet.svg");
            sheetSvg = SVG.getFromInputStream(svgInputStream);
            svgInputStream.close();

            // Load XML
            InputStream xmlInputStream = context.getAssets().open("sheet.xml");
            parseXml(xmlInputStream);
            xmlInputStream.close();
            
            Log.d(TAG, "SVG and XML assets loaded successfully. Sprites found: " + spriteRegions.size());
        } catch (IOException | SVGParseException e) {
            Log.e(TAG, "Error loading SVG/XML assets", e);
        }
    }

    private void parseXml(InputStream inputStream) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "SubTexture".equals(parser.getName())) {
                    String name = parser.getAttributeValue(null, "name");
                    float x = Float.parseFloat(parser.getAttributeValue(null, "x"));
                    float y = Float.parseFloat(parser.getAttributeValue(null, "y"));
                    float width = Float.parseFloat(parser.getAttributeValue(null, "width"));
                    float height = Float.parseFloat(parser.getAttributeValue(null, "height"));
                    spriteRegions.put(name, new SpriteRegion(x, y, width, height));
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException | NumberFormatException e) {
            Log.e(TAG, "Error parsing sheet.xml", e);
        }
    }


    public Bitmap getSprite(String name) {
        if (bitmapCache.containsKey(name)) {
            return bitmapCache.get(name);
        }

        SpriteRegion region = spriteRegions.get(name);
        if (region == null || sheetSvg == null) {
            Log.w(TAG, "Sprite not found: " + name);
            return null;
        }

        try {
            // Render the region of the SVG into a Bitmap
            Bitmap bitmap = Bitmap.createBitmap((int) region.width, (int) region.height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Log.d(TAG, "Rendering sprite: " + name + " region=" + region.x + "," + region.y + " " + region.width + "x" + region.height);
            
            // Fix: The XML might have wrong coordinates. If it looks cut, we try to find the actual paths.
            // Since we can't easily parse all paths at runtime, we'll try a small offset search
            // OR use the fixed XML if we generate it. 
            // For now, let's use the fixed XML logic directly if possible.
            
            sheetSvg.setDocumentViewBox(region.x, region.y, region.width, region.height);
            sheetSvg.setDocumentWidth(region.width);
            sheetSvg.setDocumentHeight(region.height);
            sheetSvg.renderToCanvas(canvas);

            bitmapCache.put(name, bitmap);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error rendering sprite: " + name, e);
            return null;
        }
    }

    public void clearCache() {
        for (Bitmap bitmap : bitmapCache.values()) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        bitmapCache.clear();
    }
}
