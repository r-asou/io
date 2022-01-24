package rsb.io.files;

import java.io.File;
import java.util.function.Consumer;

public interface FilesystemFileSync {

    void start(File source, Consumer<byte[]> handler)  ;
}
