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
        Scanner scanner = new Scanner(System.in);
        preLoginRepl(scanner);
    }

    private void preLoginRepl(Scanner scanner) {
        var result = "";
        System.out.print(preLoginClient.help());
        printPrompt();

        while (!result.equals("quit")) {
            String line = scanner.nextLine();
            try {
                result = preLoginClient.eval(line);
                System.out.println(result);
                if (result.equals("postLogin")) {
                    postLoginRepl(scanner);
                    System.out.print(preLoginClient.help());
                }
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }

            printPrompt();
        }
    }

    private void postLoginRepl(Scanner scanner) {
        var result = "";
        System.out.print(postLoginClient.help());
        printPrompt();

        while (!result.equals("loggedOut")) {
            String line = scanner.nextLine();
            try {
                result = postLoginClient.eval(line);
                System.out.println(result);
                if (result.equals("play") || result.equals("observe")) {
                    gameplayRepl(scanner);
                    System.out.print(postLoginClient.help());
                }
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }

            printPrompt();
        }
    }

    private void gameplayRepl(Scanner scanner) {
        var result = "";
        System.out.println("You are now in gameplay mode. Type 'leave' or 'resign' to exit.");
        printPrompt();

        while (!result.equals("quit") && !result.equals("leave") && !result.equals("resign")) {
            String line = scanner.nextLine();
            try {
                result = gameClient.eval(line);
                System.out.println(result);
            } catch (Throwable e) {
                System.out.println(e.getMessage());
            }

            printPrompt();
        }

        System.out.println("Exited gameplay mode.");
    }

    private void printPrompt() {
        System.out.print("\n>>> ");
    }
}
