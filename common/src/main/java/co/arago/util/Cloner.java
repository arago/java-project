package co.arago.util;

import java.io.*;

/**
 * Deep cloning of serializable values (slow).
 */
public class Cloner {
    /**
     * Clone any Serializable object.
     *
     * @param source The object to clone.
     * @param clazz  The class to cast the result to.
     * @param <T>    Type of the result.
     * @return The clone of source or null if source is null.
     * @throws IOException            When source is not serializable.
     * @throws ClassNotFoundException When casting to clazz fails.
     */
    public static <T> T clone(T source, Class<T> clazz) throws IOException, ClassNotFoundException {
        if (source == null)
            return null;

        if (!(source instanceof Serializable))
            throw new IOException("Cannot clone non serializable value");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(source);
            oos.flush();

            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return clazz.cast(ois.readObject());
            }
        }
    }
}
