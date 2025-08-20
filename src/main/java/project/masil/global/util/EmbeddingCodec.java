package project.masil.global.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

  public static float[] fromBytes(byte[] bytes) {
    ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    float[] arr = new float[bytes.length / 4];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = buf.getFloat();
    }
    return arr;
  }
}