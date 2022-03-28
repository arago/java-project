package co.arago.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GetByPathTest {

    private static class ClassData {
        public String classKey = "ClassKeyValue";
        private final String hiddenClassKey = "ClassKeyValue2";
    }

    private final Map<String, Object> data = new HashMap<>();

    @BeforeEach
    void setUp() {
        data.putAll(Map.of("key1", "value1",
                "key2", Map.of("key21", "value21", "key22", "value22", "class", new ClassData())));
        data.put("key3", null);
    }

    @Test
    void getWithoutExceptionGood() {
        Object result = GetByPath.newWith("/key2/key22").get(data);
        assertEquals(result, "value22");

        result = GetByPath.newWith("/key2/class/classKey").get(data);
        assertEquals(result, "ClassKeyValue");
    }

    @Test
    void getWithoutExceptionFail() {
        Object result = GetByPath.newWith("/key2/key22/some").get(data);
        assertNull(result);

        result = GetByPath.newWith("/key3").get(data);
        assertNull(result);

        result = GetByPath.newWith("/key4").get(data);
        assertNull(result);

        result = GetByPath.newWith("/key2/class/hiddenClassKey").get(data);
        assertNull(result);

        result = GetByPath.newWith("/key2/class/wrongClassKey").get(data);
        assertNull(result);
    }

    @Test
    void getWithException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GetByPath.newWithExceptions("/key2/key22/some").get(data));

        Assertions.assertThrows(IllegalArgumentException.class, () -> GetByPath.newWithExceptions("/key4").get(data));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GetByPath.newWithExceptions("/key2/class/hiddenClassKey").get(data));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> GetByPath.newWithExceptions("/key2/class/wrongClassKey").get(data));

        Object result = GetByPath.newWithExceptions("/key3").get(data);
        assertNull(result);
    }
}