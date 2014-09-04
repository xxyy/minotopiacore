package io.github.xxyy.mtc.module.truefalse;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a true/false question
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 4.9.14
 */
public class TrueFalseQuestion implements ConfigurationSerializable {
    private final String text;
    private final boolean answer;

    public TrueFalseQuestion(String text, boolean answer) {
        this.text = text;
        this.answer = answer;
    }

    public String getText() {
        return text;
    }

    public boolean getAnswer() {
        return answer;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> rtrn = new HashMap<>();
        rtrn.put("text", text);
        rtrn.put("answer", answer);
        return rtrn;
    }

    public static TrueFalseQuestion deserialize(Map<String, Object> input) {
        Validate.isTrue(input.containsKey("text") && input.containsKey("answer"), "Missing either one of text or answer");

        return new TrueFalseQuestion(input.get("text").toString(), Boolean.parseBoolean(input.get("answer").toString()));
    }
}