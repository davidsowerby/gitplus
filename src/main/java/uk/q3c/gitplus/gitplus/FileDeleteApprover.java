package uk.q3c.gitplus.gitplus;

import uk.q3c.gitplus.local.GitLocal;

import java.io.File;

/**
 * Enables a check to be made before deleting a directory or file.  For example, {@link GitLocal#cloneRemote()} may use use FileUtils.forceDelete to remove an
 * existing
 * clone.   That is not good if you have set {@link GitPlusConfiguration#projectDir(File)} to your home directory by mistake .....
 * <p>
 * Created by David Sowerby on 09 Apr 2016
 */
@FunctionalInterface
public interface FileDeleteApprover {

    boolean approve(File file);
}
