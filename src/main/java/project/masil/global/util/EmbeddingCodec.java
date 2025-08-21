package project.masil.global.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public final class EmbeddingCodec {

  private EmbeddingCodec() {
  }

  public static byte[] toBytes(List<Float> floats) {
    ByteBuffer buf = ByteBuffer.allocate(4 * floats.size()).order(ByteOrder.LITTLE_ENDIAN);
    for (Float f : floats) {
      buf.putFloat(f);
    }
    return buf.array();
  }

  public static byte[] toBytes(float[] arr) {
    ByteBuffer buf = ByteBuffer.allocate(4 * arr.length).order(ByteOrder.LITTLE_ENDIAN);
    for (float f : arr) {
      buf.putFloat(f);
    }
    return buf.array();
  }

  public static float[] fromBytes(byte[] bytes) {
    ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    float[] arr = new float[bytes.length / 4];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = buf.getFloat();
    }
    return arr;
  }

  public static List<Float> toFloatList(byte[] b) {
    ByteBuffer buf = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
    int n = b.length / 4;
    List<Float> out = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      out.add(buf.getFloat());
    }
    return out;
  }
}