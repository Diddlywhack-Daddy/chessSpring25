package clients;

import server.exceptions.BadRequestException;

import java.util.Scanner;

public class Repl {
    private final GameClient gameClient;
    private final PreLoginClient preLoginClient;
    private final PostLoginClient postLoginClient;



    public Repl(String serverURL) {
        this.gameClient = new GameClient(serverURL);
        this.preLoginClient = new PreLoginClient(serverURL);
        this.postLoginClient = new PostLoginClient(serverURL,gameClient);

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
                    postLoginClient.setAuth(preLoginClient.user,preLoginClient.auth);
                    postLoginClient.updateGameMapping();
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
        System.out.println(gameClient.help());

        try {
            gameClient.redraw();
        } catch (BadRequestException e) {
            System.out.println("Error drawing board: " + e.getMessage());
        }

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
