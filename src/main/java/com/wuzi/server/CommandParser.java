package com.wuzi.server;

import java.util.regex.Pattern;

/**
 * 命令解析器，支持十六进制坐标解析
 */
public class CommandParser {
    // 十六进制坐标模式：支持0-9, A-E, 0x前缀
    private static final Pattern HEX_PATTERN = Pattern.compile("^(0x)?[0-9A-Ea-e]$");
    
    /**
     * 解析坐标字符串为整数
     * 支持格式：
     * - 十进制：0-14
     * - 十六进制：0-E, A-E
     * - 带0x前缀：0x0-0xE
     * 
     * @param coordinate 坐标字符串
     * @return 解析后的整数坐标 (0-14)
     * @throws IllegalArgumentException 如果坐标格式无效或超出范围
     */
    public static int parseCoordinate(String coordinate) {
        if (coordinate == null || coordinate.trim().isEmpty()) {
            throw new IllegalArgumentException("坐标不能为空");
        }
        
        coordinate = coordinate.trim().toUpperCase();
        
        try {
            int value;
            
            // 处理0x前缀的十六进制
            if (coordinate.startsWith("0X")) {
                String hexPart = coordinate.substring(2);
                if (hexPart.isEmpty() || !HEX_PATTERN.matcher(hexPart).matches()) {
                    throw new IllegalArgumentException("无效的十六进制坐标格式: " + coordinate);
                }
                value = Integer.parseInt(hexPart, 16);
            }
            // 处理单个字符的十六进制 (0-9, A-E)
            else if (coordinate.length() == 1 && HEX_PATTERN.matcher(coordinate).matches()) {
                if (coordinate.matches("[0-9]")) {
                    // 十进制数字
                    value = Integer.parseInt(coordinate);
                } else {
                    // 十六进制字母 A-E
                    value = Integer.parseInt(coordinate, 16);
                }
            }
            // 处理多位十进制数字
            else if (coordinate.matches("\\d+")) {
                value = Integer.parseInt(coordinate);
            }
            else {
                throw new IllegalArgumentException("无效的坐标格式: " + coordinate);
            }
            
            // 检查坐标范围 (0-14 对应15x15棋盘)
            if (value < 0 || value > 14) {
                throw new IllegalArgumentException("坐标超出范围 (0-14): " + value);
            }
            
            return value;
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的坐标格式: " + coordinate);
        }
    }
    
    /**
     * 解析落子命令
     * 支持格式：
     * - put 5 10
     * - put A B
     * - put 0x5 0xA
     * - put 5 A
     * 
     * @param command 完整的命令字符串
     * @return 包含x,y坐标的数组
     * @throws IllegalArgumentException 如果命令格式无效
     */
    public static int[] parseMoveCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("命令不能为空");
        }
        
        String[] parts = command.trim().split("\\s+");
        
        if (parts.length != 3 || !"put".equalsIgnoreCase(parts[0])) {
            throw new IllegalArgumentException("无效的落子命令格式。正确格式: put <x坐标> <y坐标>");
        }
        
        try {
            int x = parseCoordinate(parts[1]);
            int y = parseCoordinate(parts[2]);
            return new int[]{x, y};
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("落子命令解析失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取坐标格式帮助信息
     * 
     * @return 帮助信息字符串
     */
    public static String getCoordinateHelp() {
        return "坐标格式说明:\n" +
               "- 十进制: 0-14 (例: put 5 10)\n" +
               "- 十六进制: 0-9,A-E (例: put A B)\n" +
               "- 带前缀: 0x0-0xE (例: put 0x5 0xA)\n" +
               "- 混合格式: put 5 A\n" +
               "棋盘范围: 0-14 (对应15x15棋盘)";
    }
}