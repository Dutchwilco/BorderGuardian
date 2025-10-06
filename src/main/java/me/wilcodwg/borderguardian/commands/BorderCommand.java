package me.wilcodwg.borderguardian.commands;

import me.wilcodwg.borderguardian.BorderGuardian;
import me.wilcodwg.borderguardian.managers.BorderManager;
import me.wilcodwg.borderguardian.managers.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BorderCommand implements CommandExecutor, TabCompleter {

    private final BorderGuardian plugin;
    private final ConfigManager configManager;
    private final BorderManager borderManager;

    public BorderCommand(BorderGuardian plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.borderManager = plugin.getBorderManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("borderguardian.admin")) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand.toLowerCase()) {
            case "info":
                sendBorderInfo(sender);
                break;
            case "shrink":
            case "shrik": // Common typo
                if (args.length < 2) {
                    sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.invalid-amount"));
                    return true;
                }
                handleShrink(sender, args[1]);
                break;
            case "expand":
                if (args.length < 2) {
                    sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.invalid-amount"));
                    return true;
                }
                handleExpand(sender, args[1]);
                break;
            case "center":
                if (args.length < 3) {
                    sender.sendMessage(configManager.getPrefix() + "&cUsage: /border center <x> <z>");
                    return true;
                }
                handleCenter(sender, args[1], args[2]);
                break;
            case "speed":
                if (args.length < 2) {
                    sender.sendMessage(configManager.getPrefix() + "&cUsage: /border speed <blocks_per_second>");
                    return true;
                }
                handleSpeed(sender, args[1]);
                break;
            default:
                // Try to parse as radius
                try {
                    double radius = Double.parseDouble(args[0]);
                    handleSetRadius(sender, radius);
                } catch (NumberFormatException e) {
                    sendUsage(sender);
                }
                break;
        }

        return true;
    }

    private void sendUsage(CommandSender sender) {
        List<String> usage = configManager.getMessageList("commands.usage");
        for (String line : usage) {
            sender.sendMessage(configManager.getPrefix() + line);
        }
    }

    private void sendBorderInfo(CommandSender sender) {
        List<String> info = configManager.getMessageList("commands.info");
        for (String line : info) {
            String formatted = line
                .replace("%radius%", String.valueOf((int) borderManager.getCurrentRadius()))
                .replace("%x%", String.valueOf((int) borderManager.getWorldBorder().getCenter().getX()))
                .replace("%z%", String.valueOf((int) borderManager.getWorldBorder().getCenter().getZ()))
                .replace("%world%", borderManager.getBorderWorld().getName())
                .replace("%status%", borderManager.isAnimating() ? "Animating" : "Static");
            sender.sendMessage(formatted);
        }
    }

    private void handleSetRadius(CommandSender sender, double radius) {
        if (radius < 1 || radius > 30000000) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.invalid-radius"));
            return;
        }

        borderManager.setBorderRadius(radius);
        sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.border-set",
                "%radius%", String.valueOf((int) radius)));
    }

    private void handleShrink(CommandSender sender, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.invalid-amount"));
                return;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(configManager.getPrefix() + "&cThis command can only be used by players!");
                return;
            }

            borderManager.shrinkBorder(amount, (Player) sender);
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.invalid-amount"));
        }
    }

    private void handleExpand(CommandSender sender, String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.invalid-amount"));
                return;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(configManager.getPrefix() + "&cThis command can only be used by players!");
                return;
            }

            borderManager.expandBorder(amount, (Player) sender);
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.invalid-amount"));
        }
    }

    private void handleCenter(CommandSender sender, String xStr, String zStr) {
        try {
            double x = Double.parseDouble(xStr);
            double z = Double.parseDouble(zStr);
            
            borderManager.setBorderCenter(x, z);
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.center-set",
                    "%x%", String.valueOf((int) x), "%z%", String.valueOf((int) z)));
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getPrefix() + "&cInvalid coordinates! Please use numbers.");
        }
    }

    private void handleSpeed(CommandSender sender, String speedStr) {
        try {
            double speed = Double.parseDouble(speedStr);
            if (speed <= 0) {
                sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.invalid-speed"));
                return;
            }
            
            borderManager.setAnimationSpeed(speed);
            sender.sendMessage(configManager.getPrefix() + configManager.getMessage("commands.speed-set",
                    "%speed%", String.valueOf(speed)));
        } catch (NumberFormatException e) {
            sender.sendMessage(configManager.getPrefix() + "&cInvalid speed! Please use a number.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("borderguardian.admin")) {
            return null;
        }

        if (args.length == 1) {
            return Arrays.asList("info", "shrink", "expand", "center", "speed", "1000", "2000", "5000")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("shrink") || args[0].equalsIgnoreCase("expand"))) {
            return Arrays.asList("10", "50", "100", "500");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("speed")) {
            return Arrays.asList("0.3333", "0.5", "1", "2", "5");
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("center")) {
            return Arrays.asList("0", "100", "-100");
        }

        return null;
    }
}