package clients;

import clients.ChessClient;
import clients.PostLoginClient;
import clients.PreLoginClient;
import ui.EscapeSequences;

import java.util.Scanner;

public class Repl {
    private final ChessClient chessClient;
    private final PreLoginClient preLoginClient;
    private final PostLoginClient postLoginClient;

    public Repl(String serverURL) {

        this.chessClient = new ChessClient(serverURL);
        this.preLoginClient = new PreLoginClient(serverURL);
        this.postLoginClient = new PostLoginClient(serverURL);
    }

    public void run() {

        Scanner scanner=new Scanner(System.in);
        var result="";
        while (!result.equals("quit")) {

        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + ">>> ");
    }
}