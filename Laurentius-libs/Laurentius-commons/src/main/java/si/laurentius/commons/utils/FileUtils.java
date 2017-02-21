/*
 * Copyright 2017, Supreme Court Republic of Slovenia
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package si.laurentius.commons.utils;

import java.io.File;
import java.io.IOException;
import static java.lang.String.format;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 *
 * @author Joze Rihtarsic <joze.rihtarsic@sodisce.si>
 */
public class FileUtils {
  
  
  public static void backupFile(File fToBackup) throws IOException{
 
      int i = 1;
      String fileFormat = fToBackup.getAbsolutePath() + ".%03d";
      File fileTarget = new File(format(fileFormat, i++));
      
      while (fileTarget.exists()) {
        fileTarget = new File(format(fileFormat, i++));
      }
      
      move(fToBackup.toPath(), fileTarget.toPath(), REPLACE_EXISTING);
  }
}
