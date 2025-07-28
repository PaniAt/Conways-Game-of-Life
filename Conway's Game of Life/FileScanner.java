/**
 * Scans a file and returns the contents.
 * Modifed version of my old code from (exactly) 4 months ago.
 *
 * @author Atreya Pandit
 * @version 17/06/2025
 */
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
public class FileScanner
{
    /**
     * You do not need to create a FileScanner class, as all the methods are in static context.
     */
    public FileScanner(){}

    /**
     * Returns out the contents of an entered .txt file.
     *
     * @param  fileName - The name of the file to be scanned
     * @return The contents of the file
     */
    static public String readFile(String fileName)
    {
        // Gets the file
        File targetFile = new File(fileName);
        String fileText = "";
        try {
            Scanner fileReader = new Scanner(targetFile);
            while (fileReader.hasNextLine()){
                fileText = fileText+fileReader.nextLine();
            }
        }
        catch (IOException e) { // The file could not be found.
            return "NoFileError";
        }
        return fileText;
    }
}