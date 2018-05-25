package xyz.romros.javajavascript_maven_plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "run")
public class RunMojo extends AbstractMojo {

  private static final String DEFAULT_DIR = "/src/scripts/javascript/";
  private static final String DEFAULT_JS_ENGINE = "nashorn";

  @Parameter(required = true)
  private List<String> scripts;

  @Parameter(defaultValue = "false")
  private boolean skip;

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Parameter(defaultValue = "${mojoExecution}", readonly = true)
  private MojoExecution mojoExecution;

  @Parameter(defaultValue = "${session}", readonly = true)
  private MavenSession session;

  @Override
  public void execute() throws MojoExecutionException {

    if (this.skip) { return; }
    if (this.scripts == null || this.scripts.isEmpty()) { return; }

    final ScriptEngineManager scriptEngineManager = new ScriptEngineManager(null);
    final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(DEFAULT_JS_ENGINE);

    scriptEngine.put("_project", this.project);
    scriptEngine.put("_session", this.session);
    scriptEngine.put("_log", this.getLog());

    try {
      this.interpretScripts(scriptEngine, this.scripts);
    }
    catch (final RuntimeException e) {
      if (e.getCause() instanceof MojoExecutionException) { throw (MojoExecutionException) e.getCause(); }

      throw e;
    }
    catch (final Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }

  }

  private void interpretScript(final ScriptEngine scriptEngine, final String script) {

    final Log log = this.getLog();

    final String path = this.project.getBasedir() + DEFAULT_DIR + script;
    final File scriptFile = new File(path);

    log.debug("Blalal" + path); //TODO

    scriptEngine.put(ScriptEngine.FILENAME, scriptFile.getAbsolutePath());

    try (InputStream in = new FileInputStream(scriptFile); Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
      scriptEngine.eval(reader);
    }
    catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void interpretScripts(final ScriptEngine scriptEngine, final List<String> scripts) {

    scripts.stream().forEach((script) -> this.interpretScript(scriptEngine, script));

  }

}
