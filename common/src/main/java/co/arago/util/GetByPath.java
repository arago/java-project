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
    protected final boolean forceAccess;
    protected final boolean logErrors;

    public static final class Flags {
        private boolean throwExceptions = false;
        private boolean forceAccess = false;
        private boolean logErrors = false;

        public Flags setThrowExceptions(boolean throwExceptions) {
            this.throwExceptions = throwExceptions;
            return this;
        }

        public Flags setForceAccess(boolean forceAccess) {
            this.forceAccess = forceAccess;
            return this;
        }

        public Flags setLogErrors(boolean logErrors) {
            this.logErrors = logErrors;
            return this;
        }
    }

    /**
     * Protected constructor.
     * <p>
     * Use {@link #newWith(String)} or {@link #newWith(String, EscapingStringTokenizer)}.
     *
     * @param path            The path to use. Example "/data/field".
     * @param stringTokenizer The Tokenizer to use. Arbitrary escape and delimiters can be set via this.
     * @param flags           Configuration flags.
     */
    protected GetByPath(String path, EscapingStringTokenizer stringTokenizer, Flags flags) {
        this.path = path;
        this.throwExceptions = flags.throwExceptions;
        this.forceAccess = flags.forceAccess;
        this.logErrors = flags.logErrors;

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
     * @param flags           Configuration flags.
     * @return New instance of {@link GetByPath}
     */
    public static GetByPath newWith(
            String path,
            EscapingStringTokenizer stringTokenizer,
            Flags flags) {
        return new GetByPath(path, stringTokenizer, flags);
    }

    /**
     * Static constructor
     *
     * @param path            The path to use. Example "/data/field".
     * @param stringTokenizer The Tokenizer to use. Arbitrary escape and delimiters can be set via this.
     * @return New instance of {@link GetByPath}
     */
    public static GetByPath newWith(
            String path,
            EscapingStringTokenizer stringTokenizer) {
        return newWith(
                path,
                stringTokenizer,
                new Flags());
    }

    /**
     * Static constructor. Uses default tokenizer with delimiter '/' and escape char '\'.
     *
     * @param path  The path to use. Example "/data/field".
     * @param flags Configuration flags.
     * @return New instance of {@link GetByPath}
     */
    public static GetByPath newWith(String path, Flags flags) {
        return newWith(
                path,
                EscapingStringTokenizer.newInstance()
                        .setIncludeEmpty(false)
                        .setDelimiter('/')
                        .setEscape('\\'),
                flags);
    }

    /**
     * Static constructor. Uses default tokenizer with delimiter '/' and escape char '\'.
     *
     * @param path The path to use. Example "/data/field".
     * @return New instance of {@link GetByPath}
     */
    public static GetByPath newWith(String path) {
        return newWith(
                path,
                new Flags());
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

        String errorMessage = null;
        if (scannedData instanceof Map) {
            Map<?, ?> scannedMap = ((Map<?, ?>) scannedData);
            Object value = scannedMap.get(currentName);
            if (value != null || scannedMap.containsKey(currentName)) {
                return getByNameArray(nameArray, value);
            }
        } else if (scannedData instanceof Collection) {
            Object[] scannedArray = ((Collection<?>) scannedData).toArray();

            if (scannedArray.length > 0) {
                if (StringUtils.equals(currentName, ":last")) {
                    return getByNameArray(nameArray, scannedArray[scannedArray.length - 1]);
                } else {
                    int pos = Integer.parseInt(currentName);
                    if (pos > 0 && pos < scannedArray.length) {
                        return getByNameArray(nameArray, scannedArray[pos]);
                    } else {
                        errorMessage = String.format("Index '%s' of path '%s' is out of bounds for '%s' of length %d.",
                                currentName,
                                path, scannedData.getClass().getName(), scannedArray.length);
                    }
                }
            } else {
                errorMessage = String.format("Cannot access '%s' of path '%s' for '%s' because it is empty.", currentName,
                        path, scannedData.getClass().getName());
            }
        } else if (!(scannedData instanceof String) &&
                !(scannedData instanceof Number) &&
                !(scannedData instanceof Boolean)) {
            Field field = Reflections.findFieldByName(scannedData.getClass(), currentName);
            if (field != null) {
                try {
                    if (forceAccess)
                        field.setAccessible(true);
                    return getByNameArray(nameArray, field.get(scannedData));
                } catch (IllegalAccessError | IllegalAccessException e) {
                    errorMessage = String.format("Field '%s' of '%s' is not accessible. %s", currentName,
                            scannedData.getClass().getName(), e.getMessage());
                }
            }
        } else {
            errorMessage = String.format("Cannot access '%s' of path '%s': Path too deep, no more data to scan.",
                    currentName, path);
        }

        if (throwExceptions) {
            if (StringUtils.isBlank(errorMessage))
                errorMessage = String.format("Cannot find '%s' of path '%s' in '%s'.",
                        currentName,
                        path,
                        scannedData.getClass().getName());
            if (logErrors)
                log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        } else if (StringUtils.isNotBlank(errorMessage) && logErrors) {
            log.warn("Returning null because of: " + errorMessage);
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
     * Constructor {@link GetByPath#GetByPath(String, EscapingStringTokenizer, Flags)} of this class.
     *
     * @param data The data that is searched with the {@link #path}.
     * @return The value pointed at by path or null when nothing can be found.
     * @throws IllegalArgumentException When the path does not match the scanned object and {@link #throwExceptions} is
     *                                  true.
     */
    public Object get(Object data) {
        if (StringUtils.equals(path, "/"))
            return data;

        return getByNameArray(splitPath, data);
    }
}
