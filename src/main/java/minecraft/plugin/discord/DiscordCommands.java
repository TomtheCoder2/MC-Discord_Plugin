package minecraft.plugin.discord;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.util.*;

import static minecraft.plugin.DiscordPlugin.error_log_channel;
import static minecraft.plugin.DiscordPlugin.prefix;
import static minecraft.plugin.utils.Log.*;
import static minecraft.plugin.utils.Utils.tooFewArguments;

/**
 * Represents a registry of minecraft.plugin.commands
 */
public class DiscordCommands implements MessageCreateListener {
    public HashMap<String, Command> registry = new HashMap<>();
    public HashMap<String, Command> aliasRegistry = new HashMap<>();
    private final Set<MessageCreatedListener> messageCreatedListenerRegistry = new HashSet<>();


    public DiscordCommands() {
        // stuff
    }

    // you can override the name of the command manually, for example for aliases

    /**
     * Register a command in the CommandRegistry
     *
     * @param c The command
     */
    public void registerCommand(Command c) {
        registry.put(c.name.toLowerCase(), c);

        for (String alias : c.aliases) {
            aliasRegistry.put(alias.toLowerCase(), c);
        }
    }

    /**
     * Register a command in the CommandRegistry
     *
     * @param forcedName Register the command under another name
     * @param c          The command to register
     */
    public void registerCommand(String forcedName, Command c) {
        registry.put(forcedName.toLowerCase(), c);
    }

    /**
     * Register a method to be run when a message is created.
     *
     * @param listener MessageCreatedListener to be run when a message is created.
     */
    public void registerOnMessage(MessageCreatedListener listener) {
        messageCreatedListenerRegistry.add(listener);
    }

    /**
     * Parse and run a command
     *
     * @param event Source event associated with the message
     */
    public void onMessageCreate(MessageCreateEvent event) {
        for (MessageCreatedListener listener : messageCreatedListenerRegistry) listener.run(event);
        String message = event.getMessageContent();

        // check if it's a command
        if (!message.startsWith(prefix)) return;
        // get the arguments for the command
        String[] args = message.split(" ");
        int commandLength = args[0].length();
        args[0] = args[0].substring(prefix.length());
        // command name
        String name = args[0];
        if (!isCommand(name)) return;

        // the message without the command name and the prefix
        String newMessage = null;
        if (args.length > 1) newMessage = message.substring(commandLength + 1);

        // run the command
        runCommand(name, new Context(event, args, newMessage));
    }

    /**
     * Run a command
     *
     * @param name the name of the command
     * @param ctx  the context of the command
     */
    public void runCommand(String name, Context ctx) {
        Command command = registry.getOrDefault(name.toLowerCase(), aliasRegistry.get(name.toLowerCase()));
        if (command == null) {
            return;
        }
        if (!command.hasPermission(ctx)) {
            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("No permissions!")
                    .setDescription("You need higher permissions to execute this command.");
            ctx.channel.sendMessage(eb);
            return;
        }
        if (command.minArguments > ctx.args.length - 1) {
            tooFewArguments(ctx, command);
            return;
        }
        try {
            command.run(ctx);
        } catch (Exception error) {
            log(Arrays.toString(error.getStackTrace()));
            log(error.getMessage());
            try {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("There was an error executing this command: " + name + "!")
                        .setDescription(error.getStackTrace()[0].toString())
                        .setColor(Color.decode("#ff0000"));
                error_log_channel.sendMessage(eb);
            } catch (Exception error2) {
                log("There was an error at outputting the error!!!");
                log(error2.toString());
            }
        }
    }

    /**
     * Get a command by name
     *
     * @param name the requested command name
     * @return the command
     */
    public Command getCommand(String name) {
        return registry.get(name.toLowerCase());
    }

    /**
     * Get all minecraft.plugin.commands in the registry
     *
     * @return all minecraft.plugin.commands
     */
    public Collection<Command> getAllCommands() {
        return registry.values();
    }

    /**
     * Check if a command exists in the registry
     *
     * @param name command name
     * @return return true if there is a command, else return false
     */
    public boolean isCommand(String name) {
        return registry.containsKey(name.toLowerCase()) || aliasRegistry.containsKey(name.toLowerCase());
    }
}