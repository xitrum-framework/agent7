package agent7;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html */
public class Agent {
  /** Called when the agent is loaded via command line (-javaagent:jarpath[=options]). */
  public static void premain(String agentArgs, Instrumentation inst) {
    initialize(agentArgs, inst);
  }

  /** Called when the agent is loaded after JVM startup (attach). */
  public static void agentmain(String agentArgs, Instrumentation inst) {
    initialize(agentArgs, inst);
  }

  //----------------------------------------------------------------------------

  private static Instrumentation inst = null;

  private static void initialize(String agentArgs, Instrumentation inst) {
    Agent.inst = inst;

    String currentWorkingDir = System.getProperty("user.dir");
    new ClassFileWatch(currentWorkingDir, new ClassFileWatch.OnClassModify() {
      public void onModify(Path classFilePath) {
        redefineClass(classFilePath);
      }
    });
  }

  private static void redefineClass(Path classFilePath) {
    String fileName      = classFilePath.toString();
    String withoutExt    = fileName.substring(0, fileName.length() - ".class".length());
    String classNameLike = withoutExt.replace(File.separatorChar, '.');

    // Take out matches from loaded classes
    Class<?>[] loadedClasses = inst.getAllLoadedClasses();
    ArrayList<Class<?>> matches = new ArrayList<Class<?>>();
    for (Class<?> clazz : loadedClasses) {
      String className = clazz.getName();
      if (classNameLike.endsWith(className)) {
        // "demos.action.SiteIndex" should match "classes.demos.action.SiteIndex",
        // but should not match "classes.xdemos.action.SiteIndex"
        if (classNameLike.charAt(classNameLike.length() - className.length() - 1) == '.')
          matches.add(clazz);
      }
    }

    if (matches.isEmpty()) return;

    // Take out the most suitable match (longest class name)
    Collections.sort(matches, new Comparator<Class<?>>() {
      public int compare(Class<?> c1, Class<?> c2) {
        int length1 = c1.getName().length();
        int length2 = c2.getName().length();
        return length2 - length1;
      }
    });
    Class<?> clazz = matches.get(0);

    // Actually reload
    try {
      ClassDefinition definition = new ClassDefinition(clazz, readFile(fileName));
      inst.redefineClasses(new ClassDefinition[] {definition});
      System.out.println("Reloaded: " + clazz.getName());
    } catch (Exception e) {
      System.out.println("Error reloading " + clazz.getName() + "(" + fileName + "): " + e.toString());
      e.printStackTrace();
    }
  }

  private static byte[] readFile(String path) throws Exception {
    FileInputStream       is   = new FileInputStream(new File(path));
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    byte[] buffer = new byte[1024];
    int bytesRead = 0;
    while ((bytesRead = is.read(buffer)) != -1) baos.write(buffer, 0, bytesRead);

    byte[] result = baos.toByteArray();
    baos.close();
    is.close();
    return result;
  }
}
