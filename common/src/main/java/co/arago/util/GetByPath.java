package co.arago.util;

import co.arago.util.reflections.Reflections;
import co.arago.util.text.EscapingStringTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Get an arbitrary field from any Object by using a Path. This is using Reflections on non POJO objects.
 */
public class GetByPath {
    private final static Logger log = LoggerFactory.getLogger(GetByPath.class);

    protected final String path;
    protected final List<String> splitPath;
    protected final boolean throwExceptions;

    /**
     * Protected constructor.
     * <p>
     * Use {@link #newWith(String)} or {@link #newWith(String, EscapingStringTokenizer, boolean)}.
     *
     * @param path            The path to use. Example "/data/field".
     * @param stringTokenizer The Tokenizer to use. Arbitrary escape and delimiters can be set via this.
     * @param throwExceptions Whether to throw exceptions when paths do not match the scanned object.
     */
    protected GetByPath(
            String path,
            EscapingStringTokenizer stringTokenizer,
            boolean throwExceptions) {
        this.path = path;
        this.throwExceptions = throwExceptions;

        if (path != null && path.charAt(0) == stringTokenizer.delimiter) {
            splitPath = stringTokenizer.build(path);
        } else {
            splitPath = new ArrayList<>();
        }
    }

    /**
     * Static constructor
     *
     * @param path            The path to use. Example "/data/field".
     * @param stringTokenizer The Tokenizer to use. Arbitrary escape and delimiters can be set via this.
     * @param throwExceptions Whether to throw exceptions when paths do not match the scanned object.
     * @return New instance of {@link GetByPath}
     */
    public static GetByPath newWith(
            String path,
            EscapingStringTokenizer stringTokenizer,
            boolean throwExceptions) {
        return new GetByPath(path, stringTokenizer, throwExceptions);
    }

    /**
     * Static constructor. Uses default tokenizer with delimiter '/' and escape char '\'.
     *
     * @param path The path to use. Example "/data/field".
     * @return New instance of {@link GetByPath}
     */
    public static GetByPath newWithExceptions(
            String path) {
        return newWith(path, EscapingStringTokenizer.newInstance()
                .setIncludeEmpty(false)
                .setDelimiter('/')
                .setEscape('\\'),
                true);
    }

    /**
     * Static constructor. Uses default tokenizer with delimiter '/' and escape char '\' and
     * {@link #throwExceptions} = false.
     *
     * @param path The path to use. Example "/data/field".
     * @return New instance of {@link GetByPath}
     */
    public static GetByPath newWith(String path) {
        return newWith(path, EscapingStringTokenizer.newInstance()
                .setIncludeEmpty(false)
                .setDelimiter('/')
                .setEscape('\\'),
                false);
    }

    /**
     * Use nameArray to fetch the next field in scannedData recursively.
     * <ul>
     * <li>When nameArray is empty we are finished and scannedData is returned as result.</li>
     * <li>Pop currentPath from nameArray</li>
     * <li>When scannedData is a Map, the currentPath is the key for the next part,</li>
     * <li>when scannedData is a Collection, the currentPath will be translated into an integer and
     * be used as index for the Collection, or when the key is ':last', the last entry of the array will be
     * returned,</li>
     * <li>otherwise when scannedData is not null, the next part will be searched by looking for a
     * field in the class of scannedData with the currentPath as name by using Reflections,</li>
     * <li>when scannedData is null, no match has been found and null is returned.</li>
     * </ul>
     *
     * @param nameArray   Array with keys/indices/fieldNames (depending on scannedData) for scannedData.
     * @param scannedData The data object do scan through by using nameArray.
     * @return The data pointed at by nameArray or null when nothing can be found at path and {@link #throwExceptions}
     *         is false.
     * @throws IllegalArgumentException When the path does not match the scanned object and {@link #throwExceptions} is
     *                                  true.
     */
    public Object getByNameArray(List<String> nameArray, Object scannedData) {
        if (nameArray == null || nameArray.isEmpty())
            return scannedData;

        String currentName = nameArray.remove(0);

        if (scannedData instanceof Map) {
            Map<?, ?> scannedMap = ((Map<?, ?>) scannedData);
            Object value = scannedMap.get(currentName);
            if (value != null || scannedMap.containsKey(currentName)) {
                return getByNameArray(nameArray, value);
            }
        } else if (scannedData instanceof Collection) {
            Collection<?> scannedCollection = ((Collection<?>) scannedData);
            Object[] scannedArray = ((Collection<?>) scannedData).toArray();

            if (scannedArray.length > 0) {
                if (StringUtils.equals(currentName, ":last")) {
                    return getByNameArray(nameArray, scannedArray[scannedArray.length - 1]);
                } else {
                    int pos = Integer.parseInt(currentName);
                    if (pos > 0 && pos < scannedArray.length) {
                        return getByNameArray(nameArray, pos);
                    }
                }
            }
        } else if (scannedData != null) {
            Field field = Reflections.findFieldByName(scannedData.getClass(), currentName);
            if (field != null) {
                try {
//                    field.setAccessible(true);
                    return getByNameArray(nameArray, field.get(scannedData));
                } catch (IllegalAccessError | IllegalAccessException e) {
                    log.error("Field '{}' of '{}' is not accessible. {}", currentName, scannedData.getClass().getName(),
                            e.getMessage());
                }
            }
        }

        if (this.throwExceptions) {
            throw new IllegalArgumentException(
                    String.format("Cannot find '%s' of '%s' in '%s'.",
                            currentName,
                            path,
                            scannedData != null ? scannedData.getClass().getName() : "null"));
        }

        return null;
    }

    /**
     * QuickPath. This fetches a field in data by using a path
     * like a UNIX directory path. The path has to start with '/',
     * delimiter for the names of the path is '/', escape character is '\\',
     * i.e '\\/' for a literal '/' as part of a name inside the path.
     * <p>
     * Delimiter and escape characters can be changed by supplying your own {@link EscapingStringTokenizer} in the
     * Constructor {@link GetByPath#GetByPath(String, EscapingStringTokenizer, boolean)} of this class.
     *
     * @param data The data that is searched with the {@link #path}.
     * @return The value pointed at by path or null when nothing can be found.
     * @throws IllegalArgumentException When the path does not match the scanned object and {@link #throwExceptions} is
     *                                  true.
     */
    public Object get(Object data) {
        if (StringUtils.equals(path, "/"))
            return data;

        try {
            return getByNameArray(splitPath, data);
        } catch (Exception e) {
            if (this.throwExceptions)
                throw e;
            log.error("Error while applying path '{}' to '{}': {}", path, data.getClass().getName(), e);
            return null;
        }
    }
}
