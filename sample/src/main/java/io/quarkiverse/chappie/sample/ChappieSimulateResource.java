package io.quarkiverse.chappie.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/chappie/simulate")
public class ChappieSimulateResource {

    @GET
    @Path("/nullpointer")
    public int nullpointer() {
        String str = "";
        return str.length();
    }

    @GET
    @Path("/arrayindexoutofbounce")
    public int arrayindexoutofbounce() {
        int[] arr = new int[5];
        return arr[10];
    }

    @GET
    @Path("/arithmetic")
    public int arithmetic() {
        int a = 10;
        int b = 0;
        return a / b;
    }

    @GET
    @Path("/filenotfound")
    public int filenotfound() throws FileNotFoundException {
        File file = new File("nonexistentfile.txt");
        FileReader fr = new FileReader(file);
        return 0;
    }

    @GET
    @Path("/classnotfound")
    public int classnotfound() throws ClassNotFoundException {
        Class.forName("non.existent.ClassName");
        return 0;
    }

    @GET
    @Path("/numberformat")
    public int numberformat() {
        String invalidNumber = "abc";
        return Integer.parseInt(invalidNumber);
    }
}
