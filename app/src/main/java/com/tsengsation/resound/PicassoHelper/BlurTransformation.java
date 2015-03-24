package com.tsengsation.resound.PicassoHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

/**
 * Created by jonathantseng on 3/24/15.
 */
public class BlurTransformation implements Transformation {

    private final static int BLUR_RADIUS = 24;

    private RenderScript mRenderScript;

    public BlurTransformation(Context context) {
        this.mRenderScript = RenderScript.create(context);
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Allocation input = Allocation.createFromBitmap(mRenderScript, source, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SHARED);
        Allocation output = Allocation.createTyped(mRenderScript, input.getType());
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
        script.setRadius(BLUR_RADIUS);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(source);
        return source;
    }

    @Override
    public String key() {
        return "blur";
    }
}
