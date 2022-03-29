package co.arago.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GetByPathTest {

    private static class ClassData {
        public String classKey = "ClassKeyValue";
        private final String hiddenClassKey = "ClassKeyValue2";
    }

    private final Map<String, Object> data = new HashMap<>();
    private final GetByPath.Flags logFlags = new GetByPath.Flags().setLogErrors(true);
    private final GetByPath.Flags exFlags = new GetByPath.Flags().setThrowExceptions(true).setLogErrors(true);

    @BeforeEach
    void setUp() {
        data.putAll(Map.of("key1", "value1",
                "key2", Map.of("key21", "value21", "key22", "value22", "class", new ClassData()),
                "list", List.of("A", "B", "C", "D"),
                "list2", List.of()));
        data.put("key3", null);
    }

    @Test
    void getWithoutExceptionGood() {
        Object result = GetByPath.newWith("/key2/key22", logFlags).get(data);
        assertEquals(result, "value22");

        result = GetByPath.newWith("/key2/class/classKey", logFlags).get(data);
        assertEquals(result, "ClassKeyValue");

        result = GetByPath.newWith("/list/:last", logFlags).get(data);
        assertEquals(result, "D");

        result = GetByPath.newWith("/list/1", logFlags).get(data);
        assertEquals(result, "B");
    }

    @Test
    void getWithoutExceptionFail() {
        Object result = GetByPath.newWith("/key2/key22/some", logFlags).get(data);
        assertNull(result);

        result = GetByPath.newWith("/key3", logFlags).get(data);
        assertNull(result);

        result = GetByPath.newWith("/key4", logFlags).get(data);
        assertNull(result);

        result = GetByPath.newWith("/key2/class/hiddenClassKey", logFlags).get(data);
        assertNull(result);

        result = GetByPath.newWith("/key2/class/wrongClassKey", logFlags).get(data);
        assertNull(result);

        result = GetByPath.newWith("/list/4", logFlags).get(data);
        assertNull(result);

        result = GetByPath.newWith("/list2/:last", logFlags).get(data);
        assertNull(result);
    }

    @Test
    void getWithException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GetByPath.newWith("/key2/key22/some", exFlags).get(data));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GetByPath.newWith("/key4", exFlags).get(data));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GetByPath.newWith("/key2/class/hiddenClassKey", exFlags).get(data));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GetByPath.newWith("/key2/class/wrongClassKey", exFlags).get(data));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GetByPath.newWith("/list/4", exFlags).get(data));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GetByPath.newWith("/list2/:last", exFlags).get(data));

        Object result = GetByPath.newWith("/key3", exFlags).get(data);
        assertNull(result);

    }
}