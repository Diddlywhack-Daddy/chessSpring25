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

    // This is the first layer, the pre-login REPL
    private void preLoginRepl(Scanner scanner) {
        var result = "";
        System.out.print(preLoginClient.help());
        printPrompt();
        while (!result.equals("quit")) {
            String line = scanner.nextLine();
            try {
                result = preLoginClient.eval(line);
                if (result.equals("postLogin")) {
                    postLoginRepl(scanner);
                    System.out.print(preLoginClient.help());
                } else {
                    System.out.print(result);
                }
                printPrompt();
            } catch (Throwable e) {
                System.out.print(e.toString());
                printPrompt();
            }
        }
        System.out.println();
    }

    // This is the second layer, the post-login REPL
    private void postLoginRepl(Scanner scanner) {
        var result = "";
        System.out.print(postLoginClient.help());
        printPrompt();
        while (!result.equals("loggedOut")) {
            String line = scanner.nextLine();
            try {
                result = postLoginClient.eval(line);
                if (result.equals("play") || result.equals("observe")) {
                    gameplayRepl(scanner);
                    System.out.print(postLoginClient.help());
                } else {
                    System.out.print(result);
                }
                printPrompt();
            } catch (Throwable e) {
                System.out.print(e.toString());
                printPrompt();
            }
        }
        System.out.println();
    }

    // This is the final layer, the gameplay REPL
    private void gameplayRepl(Scanner scanner) {
        // Placeholder for in-game interaction (move input, resign, redraw board, etc.)
        System.out.println("Entered gameplay mode. Type 'resign' or 'exit' to leave game mode.");
        printPrompt();
        String command = "";
        while (!command.equals("resign") && !command.equals("exit")) {
            command = scanner.nextLine().trim().toLowerCase();
            // TODO: Add actual gameplay command handling here
            System.out.println("You entered: " + command);
            printPrompt();
        }
        System.out.println("Exited gameplay mode.");
    }

    private void printPrompt() {
        System.out.print("\n>>> ");
    }
}
