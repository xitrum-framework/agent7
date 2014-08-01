package agent7;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.LinkOption.*;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * See http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 * http://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
 */
class ClassFileWatch {
  interface OnClassModify {
    void onModify(Path classFilePath);
  }

  private WatchService watcher;
  private Map<WatchKey, Path> keys;
  private OnClassModify onClassModify;

  public ClassFileWatch(String dir, OnClassModify onClassModify) {
    try {
      this.watcher       = FileSystems.getDefault().newWatchService();
      this.keys          = new HashMap<WatchKey, Path>();
      this.onClassModify = onClassModify;

      Path path = Paths.get(dir);
      registerAll(path);

      Thread t = new Thread() {
        public void run() {
          try {
            processEvents();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      };
      t.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void register(Path dir) throws IOException {
    Modifier high = get_com_sun_nio_file_SensitivityWatchEventModifier_HIGH();
    WatchKey key =
      (high == null) ?
      dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY) :
      dir.register(watcher, new WatchEvent.Kind<?>[]{ENTRY_CREATE, ENTRY_MODIFY}, high);
    keys.put(key, dir);
  }

  private void registerAll(final Path start) throws IOException {
    Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir,
          BasicFileAttributes attrs) throws IOException {
        register(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void processEvents() throws IOException {
    for (;;) {
      // Wait for key to be signalled
      WatchKey key;
      try {
        key = watcher.take();
      } catch (InterruptedException x) {
        return;
      }

      Path dir = keys.get(key);
      if (dir == null) continue;

      for (WatchEvent<?> event: key.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == OVERFLOW) continue;

        // Context for directory entry event is the file name of entry
        WatchEvent<Path> ev = cast(event);
        Path name = ev.context();
        Path child = dir.resolve(name);

        // The compiler may delete and recreate .class file, not only modifying
        if (child.toFile().isFile() && child.toString().endsWith(".class"))
          onClassModify.onModify(child);

        // If directory is created, register it and its sub-directories
        if (kind == ENTRY_CREATE && Files.isDirectory(child, NOFOLLOW_LINKS)) registerAll(child);
      }

      // Reset key and remove from set if directory no longer accessible
      boolean valid = key.reset();
      if (!valid) {
        keys.remove(key);

        // All directories are inaccessible
        if (keys.isEmpty()) break;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

  // http://stackoverflow.com/questions/9588737/is-java-7-watchservice-slow-for-anyone-else
  // http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/com/sun/nio/file/SensitivityWatchEventModifier.java
  private Modifier get_com_sun_nio_file_SensitivityWatchEventModifier_HIGH() {
    try {
      Class<?> c = Class.forName("com.sun.nio.file.SensitivityWatchEventModifier");
      Field    f = c.getField("HIGH");
      return (Modifier) f.get(c);
    } catch (Exception e) {
      return null;
    }
  }
}
