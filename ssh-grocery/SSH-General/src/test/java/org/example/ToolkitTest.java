package org.example;

import org.example.Toolkit;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ToolkitTest {
    @Test
    void testGenerateShortID() {
        String realName = "Emily Yang";
        String shortID = Toolkit.generateShortID(realName);

        // Check that the shortID starts with the real name (with spaces replaced by underscores)
        assertTrue(shortID.startsWith("Emily_Yang_"));

        // Check that the length of the shortID is correct
        assertEquals(19, shortID.length());
    }

    @Test
    void askContinueYN() {
        // Test when user enters 'y'
        String clientMessage = "y";
        BufferedReader in = new BufferedReader(new StringReader(clientMessage));
        StringWriter out = new StringWriter();
        PrintWriter printWriter = new PrintWriter(out);
        assertTrue(Toolkit.askContinue(in, printWriter));

        // Test when user enters 'n'
        clientMessage = "n";
        in = new BufferedReader(new StringReader(clientMessage));
        out = new StringWriter();
        printWriter = new PrintWriter(out);
        assertFalse(Toolkit.askContinue(in, printWriter));

    }

    @Test
    void askContinueInvalidInput() throws IOException {
        // Test when user enters invalid input
        String clientMessage = "invalid input";
        BufferedReader in = new BufferedReader(new StringReader(clientMessage));
        StringWriter out = new StringWriter();
        PrintWriter printWriter = new PrintWriter(out);
        // if input is invalid, the output will be "Invalid input. Do you want to continue? y/n"
        Toolkit.askContinue(in, printWriter);
        assertEquals("Invalid input. Please only enter 'y' or 'n'.\nConnection closed or input error.\n", out.toString());
    }
}