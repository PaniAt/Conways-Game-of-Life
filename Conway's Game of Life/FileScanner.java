
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
     * Does nothing.
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
        File myFile = new File(fileName);
        String fileText = "";
        try {
            Scanner fileReader = new Scanner(myFile);
            while (fileReader.hasNextLine()){
                fileText = fileText+fileReader.nextLine();
            }
        }
        catch (IOException e) { // If there is an error with the file
            e.printStackTrace();
            System.out.println("Unable to locate file path!");
            throw new Error("FileNotFoundError");
        }
        return fileText;
    }
}
