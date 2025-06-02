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


    //This will be my first layer, the prelogin repl
    private void preLoginRepl(){

    }

    //This will be my second layer, postlogin repl. It is called by preLogin and uses break in the quit
    private void postLoginRepl(){

    }

    //This is the last layer, the gameplay repl. It is called by postLogin and uses break in the quit
    private void gameplayRepl(){

    }

    private void printPrompt() {
        System.out.print("\n" + ">>> ");
    }
}