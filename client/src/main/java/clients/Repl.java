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

    public Repl(String serverURL, ChessClient chessClient, PreLoginClient preLoginClient, PostLoginClient postLoginClient) {

        this.chessClient = chessClient;
        this.preLoginClient = preLoginClient;
        this.postLoginClient = postLoginClient;
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