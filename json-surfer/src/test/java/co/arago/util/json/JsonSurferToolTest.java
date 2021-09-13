package co.arago.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonSurferToolTest {

    private final static Logger log = LoggerFactory.getLogger(JsonSurferToolTest.class);

    static Map<String, Object> jsonData = Map.of(
            "Key1", "Value1",
            "Key2", 42,
            "Key3", List.of(1, 2, 3, 4),
            "Key4", Map.of("Hello", "World")
    );


    @Test
    void getByJsonPath() throws JsonProcessingException {
        String result = GetByJsonPath.newWith("$.Key1").get(jsonData, String.class);

        log.info(result);

        assertEquals(result, "Value1");
    }

    @Test
    void getAllByJsonPath() throws JsonProcessingException {
        Collection<Integer> result = GetByJsonPath.newWith("$.Key3[1:3]").getAll(jsonData, Integer.class);

        log.info(JsonTools.DEFAULT.toString(result));

        assertEquals(result, List.of(2, 3));
    }
}