package me.xuan.bdocr.sdk.model;

/**
 * Author: xuan
 * Created on 2019/10/23 14:50.
 * <p>
 * Describe:
 */
public class GeneralParams extends GeneralBasicParams {
    public static final String GRANULARITY_BIG = "big";
    public static final String GRANULARITY_SMALL = "small";

    public GeneralParams() {
    }

    public void setVertexesLocation(boolean vertexesLocation) {
        this.putParam("vertexes_location", vertexesLocation);
    }

    public void setRecognizeGranularity(String granularity) {
        this.putParam("recognize_granularity", granularity);
    }
}
