package info.hupel.jsr223;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

public final class LaunchConfig {

    private final String language;
    private final String script;

    public static LaunchConfig ofPath(String language, Path path, Charset charset) throws IOException {
        byte bytes[] = Files.readAllBytes(path);
        String script = new String(bytes, charset);
        return new LaunchConfig(language, script);
    }

    public static LaunchConfig ofResource(String language, ClassLoader loader, String resource, Charset charset) throws IOException {
        try {
            Path path = Paths.get(loader.getResource(resource).toURI());
            return ofPath(language, path, charset);
        }
        catch (URISyntaxException ex) {
            throw new InternalError("ClassLoader resource should be a valid URI", ex);
        }
    }

    public LaunchConfig(String language, String script) {
        this.language = language;
        this.script = script;
    }

    public String getLanguage() {
        return language;
    }

    public String getScript() {
        return script;
    }

    public void launch() throws ScriptException {
        launch(Collections.emptyMap());
    }

    public Bindings launch(Map<String, Object> initialBindings) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(language);
        Bindings bindings = engine.createBindings();
        bindings.putAll(initialBindings);
        engine.eval(script, bindings);
        return bindings;
    }

}
