package clients;

import java.util.Scanner;

public class Repl {
    private final GameClient gameClient;
    private final PreLoginClient preLoginClient;
    private final PostLoginClient postLoginClient;

    public Repl(String serverURL) {

        this.gameClient = new GameClient(serverURL);
        this.preLoginClient = new PreLoginClient(serverURL);
        this.postLoginClient = new PostLoginClient(serverURL);
    }

    public void run() {
        System.out.println("Welcome to chess.");
        Scanner scanner=new Scanner(System.in);
        preLoginRepl(scanner);

    }


    //This will be my first layer, the prelogin repl
    private void preLoginRepl(Scanner scanner){
        var result="";
        System.out.print(preLoginClient.help());
        while (!result.equals("quit")) {
            String line = scanner.nextLine();


            try {
                result = preLoginClient.eval(line);
                if(result.equals("postLogin")){
                    postLoginRepl(scanner);
                    System.out.print(preLoginClient.help());
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    //This will be my second layer, postlogin repl. It is called by preLogin and uses break in the quit
    private void postLoginRepl(Scanner scanner){
        var result="";
        System.out.print(postLoginClient.help());
        while (!result.equals("loggedOut")) {
            String line = scanner.nextLine();


            try {
                result = postLoginClient.eval(line);
                System.out.print(result);
                if(result == "login"){
                    postLoginRepl(scanner);
                    System.out.print(postLoginClient.help());
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    //This is the last layer, the gameplay repl. It is called by postLogin and uses break in the quit
    private void gameplayRepl(){

    }

    private void printPrompt() {
        System.out.print("\n" + ">>> " );
    }
}