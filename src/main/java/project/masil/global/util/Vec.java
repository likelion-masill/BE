package project.masil.global.util;

public final class Vec {

  private Vec() {
  }

  public static float[] l2norm(float[] v) {
    double s = 0;
    for (float x : v) {
      s += x * x;
    }
    if (s == 0) {
      return v.clone();
    }
    float inv = (float) (1.0 / Math.sqrt(s));
    float[] out = new float[v.length];
    for (int i = 0; i < v.length; i++) {
      out[i] = v[i] * inv;
    }
    return out;
  }

  // alpha: 업데이트 강도
  public static float[] ema(float[] u, float[] x, float alpha) {
    float[] out = new float[u.length];
    float oneMinus = 1f - alpha;
    for (int i = 0; i < u.length; i++) {
      out[i] = oneMinus * u[i] + alpha * x[i];
    }
    return l2norm(out);
  }
}