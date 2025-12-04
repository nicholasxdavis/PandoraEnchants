package com.pandora.enchants.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for paginating messages and lists
 */
public class PaginationUtil {
    
    private static final int ITEMS_PER_PAGE = 8;
    
    /**
     * Paginates a list of strings into pages
     */
    public static List<String> paginate(List<String> items, int page) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());
        
        List<String> pageItems = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            pageItems.add(items.get(i));
        }
        
        return pageItems;
    }
    
    /**
     * Gets total pages for a list
     */
    public static int getTotalPages(List<?> items) {
        if (items == null || items.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
    }
    
    /**
     * Creates a footer message for pagination
     */
    public static String createFooter(int currentPage, int totalPages) {
        if (totalPages <= 1) {
            return "";
        }
        
        StringBuilder footer = new StringBuilder();
        footer.append(ColorUtil.colorize("&7&m--------------------------------\n"));
        footer.append(ColorUtil.colorize("&7Page &6")).append(currentPage);
        footer.append(ColorUtil.colorize(" &7/ &6")).append(totalPages);
        
        if (currentPage < totalPages) {
            footer.append(ColorUtil.colorize(" &7| &e/pe list ")).append(currentPage + 1);
            footer.append(ColorUtil.colorize(" &7for next page"));
        }
        if (currentPage > 1) {
            footer.append(ColorUtil.colorize(" &7| &e/pe list ")).append(currentPage - 1);
            footer.append(ColorUtil.colorize(" &7for previous page"));
        }
        
        return footer.toString();
    }
}

