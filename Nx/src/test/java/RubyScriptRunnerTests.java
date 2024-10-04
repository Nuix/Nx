import com.nuix.innovation.enginewrapper.RubyScriptRunner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RubyScriptRunnerTests {
    @Test
    public void RunScript() throws Exception {
        // Validate that script runs and output is captured
        String script = "5.times{puts 'hello'}";
        List<String> output = new ArrayList<>();
        RubyScriptRunner rubyScriptRunner = new RubyScriptRunner();
        rubyScriptRunner.setStandardOutputConsumer(output::add);
        rubyScriptRunner.setErrorOutputConsumer(output::add);
        rubyScriptRunner.runScriptAsync(script, "0.0.0.0", Map.of());
        rubyScriptRunner.join();
        assertTrue(output.size() > 0);
        System.out.println(String.join("",output));
    }

    @Test
    public void ReturnedValue() throws Exception {
        // Test that returned value from last statement is captured
        String script = "response['sum'] = 10 + 15 + 15 + 2";
        List<String> output = new ArrayList<>();
        RubyScriptRunner rubyScriptRunner = new RubyScriptRunner();

        rubyScriptRunner.whenScriptCompletes((ret,vars) -> {
            assertEquals(42L, ret);
        });

        Map<String, Object> varContainer = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        varContainer.put("response", response);

        rubyScriptRunner.setStandardOutputConsumer(output::add);
        rubyScriptRunner.setErrorOutputConsumer(output::add);
        rubyScriptRunner.runScriptAsync(script, "0.0.0.0", varContainer);
        rubyScriptRunner.join();

        assertEquals(42L, response.get("sum"));
    }
}
