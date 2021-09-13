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

    /**
     * Protected constructor.
     * <p>
     * Use {@link #newWith(String)} or {@link #newWith(String, EscapingStringTokenizer)}.
     *
     * @param path            The path to use. Example "/data/field".
     * @param stringTokenizer The Tokenizer to use. Arbitrary escape and delimiters can be set via this.
     */
    protected GetByPath(String path, EscapingStringTokenizer stringTokenizer) {
        this.path = path;

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
     * @return New instance of {@link GetByPath}
     */
    public static GetByPath newWith(String path, EscapingStringTokenizer stringTokenizer) {
        return new GetByPath(path, stringTokenizer);
    }

    /**
     * Static constructor. Uses default tokenizer with delimiter '/' and escape char '\'.
     *
     * @param path The path to use. Example "/data/field".
     * @return New instance of {@link GetByPath}
     */
    public static GetByPath newWith(String path) {
        return newWith(path, EscapingStringTokenizer.newInstance()
                .setIncludeEmpty(false)
                .setDelimiter('/')
                .setEscape('\\'));
    }

    /**
     * Use nameArray to fetch the next field in scannedData recursively.
     * <ul>
     *     <li>When nameArray is empty we are finished and scannedData is returned as result.</li>
     *     <li>Pop currentPath from nameArray</li>
     *     <li>When scannedData is a Map, the currentPath is the key for the next part,</li>
     *     <li>when scannedData is a Collection, the currentPath will be translated into an integer and
     *     be used as index for the Collection, or when the key is ':last', the last entry of the array will be
     *     returned,</li>
     *     <li>otherwise when scannedData is not null, the next part will be searched by looking for a
     *     field in the class of scannedData with the currentPath as name by using Reflections,</li>
     *     <li>when scannedData is null, no match has been found and null is returned.</li>
     * </ul>
     *
     * @param nameArray   Array with keys/indices/fieldNames (depending on scannedData) for scannedData.
     * @param scannedData The data object do scan through by using nameArray.
     * @return The data pointed at by nameArray or null when nothing can be found at path.
     */
    public static Object getByNameArray(List<String> nameArray, Object scannedData) {
        if (nameArray == null || nameArray.isEmpty())
            return scannedData;

        String currentName = nameArray.remove(0);

        if (scannedData instanceof Map) {
            return getByNameArray(nameArray, ((Map<?, ?>) scannedData).get(currentName));
        } else if (scannedData instanceof Collection) {
            Object[] scannedArray = ((Collection<?>) scannedData).toArray();

            return getByNameArray(nameArray,
                    StringUtils.equals(currentName, ":last") ? scannedArray[scannedArray.length - 1] : scannedArray[Integer.parseInt(currentName)]
            );
        } else if (scannedData != null) {
            Field field = Reflections.findFieldByName(scannedData.getClass(), currentName);
            if (field == null)
                return null;

            try {
                field.setAccessible(true);
                return getByNameArray(nameArray, field.get(scannedData));
            } catch (IllegalAccessError | IllegalAccessException e) {
                log.error("Field '{}' of '{}' is not accessible.", currentName, scannedData.getClass().getName(), e);
            }
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
     * Constructor {@link GetByPath#GetByPath(String, EscapingStringTokenizer)} of this class.
     *
     * @param data The data that is searched with the {@link #path}.
     * @return The value pointed at by path or null when nothing can be found.
     */
    public Object get(Object data) {
        if (StringUtils.equals(path, "/"))
            return data;

        try {
            return getByNameArray(splitPath, data);
        } catch (Exception e) {
            log.error("Error while applying path '{}' to '{}': {}", path, data.getClass().getName(), e);
            return null;
        }
    }
}
