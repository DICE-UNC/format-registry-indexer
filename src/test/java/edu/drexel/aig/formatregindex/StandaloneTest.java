package edu.drexel.aig.formatregindex;

import java.io.IOException;

/**
 * Created by Isaac Simmons on 9/25/2014.
 */
public class StandaloneTest {
    public static void main(String[] args) throws IOException {
        FormatRegistryClient frc = new FormatRegistryClient();
        for (String result: frc.results("testfile.txt", StandaloneTest.class.getResourceAsStream("/testfile.txt"))) {
            System.out.println("Test result: " + result);
        }
    }
}
